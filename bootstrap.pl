#! /usr/bin/perl -w

use strict;
use FileHandle;

# Bootstrap CloudCoder on an Ubuntu server

# Download site
my $DOWNLOAD_SITE = 'https://s3.amazonaws.com/cloudcoder-binaries';

my $program = $0;
#print "program=$program\n";
#exit 0;

my $dryRun = 0;
if (scalar(@ARGV) > 0 && $ARGV[0] eq '-n') {
	print ">>> Dry run <<<\n";
	shift @ARGV;
	$dryRun = 1;
}

my $mode = 'start';

if (scalar(@ARGV) > 0) {
	$mode = shift @ARGV;
}

if ($mode eq 'start') {
	Start();
} elsif ($mode eq 'step2') {
	Step2();
} else {
	die "Unknown mode: $mode\n";
}

sub Start {
	print <<"GREET";
Welcome to the CloudCoder bootstrap script.

By running this script, you will create a basic CloudCoder
installation on a server running Ubuntu Linux.

Make sure to run this script from a user account that has
permission to run the "sudo" command.  If you see the
following prompt:

  sudo password>>

then you will need to type the account password and press
enter.  On some Ubuntu systems, such as Ubuntu server on
Amazon EC2, no password is required for sudo, so don't be
concerned if you don't see the prompt.
GREET
	
	my $readyToStart = ask("\nReady to start? (yes/no)");
	exit 0 if ((lc $readyToStart) ne 'yes');
	
	print "\nFirst, please enter some configuration information...\n\n";
	
	# Get minimal required configuration information
	my $ccUser = ask("What username do you want for your CloudCoder account?");
	my $ccPasswd = ask("What password do you want for your CloudCoder account?");
	my $ccFirstName = ask("What is your first name?");
	my $ccLastName = ask("What is your last name?");
	my $ccEmail = ask("What is your email address?");
	my $ccWebsite = ask("What is the URL of your personal website?");
	my $ccInstitutionName = ask("What is the name of your institution?");
	my $ccMysqlRootPasswd = ask("What password do you want for the MySQL root user?");
	my $ccMysqlCCPasswd = ask("What password do you want for the MySQL cloudcoder user?");
	my $ccHostname = ask("What is the hostname of this server?");

	print "\n";
	my $startInstall = ask("Are you ready to start the installation? (yes/no)");
	exit 0 if ((lc $startInstall) ne 'yes');
	
	# ----------------------------------------------------------------------
	# Install/configure required packages
	# ----------------------------------------------------------------------
	section("Installing required packages...");

	# Run apt-get update so that repository metadata is current
	RunAdmin(
		env => { 'DEBIAN_FRONTEND' => 'noninteractive' },
		cmd => ["apt-get", "update"]
	);

	# Determine which mysql-server version we will use
	my $mysqlVersion = FindMysqlVersion();
	print "Mysql version is $mysqlVersion\n";

	# Configure mysql root password so that no user interaction
	# will be required when installing packages.
	DebconfSetSelections("mysql-server-$mysqlVersion", "mysql-server/root_password", "password $ccMysqlRootPasswd");
	DebconfSetSelections("mysql-server-$mysqlVersion", "mysql-server/root_password_again", "password $ccMysqlRootPasswd");

	# Install packages. Note that because cloudcoderApp.jar is self-configuring,
	# we can install the JRE rather than the full JDK, as we won't need
	# the jar tool.
	RunAdmin(
		env => { 'DEBIAN_FRONTEND' => 'noninteractive' },
		cmd => ["apt-get", "-y", "install", "openjdk-6-jre-headless", "mysql-client-$mysqlVersion",
			"mysql-server-$mysqlVersion", "apache2"]
	);
	
	# ----------------------------------------------------------------------
	# Configure MySQL
	# ----------------------------------------------------------------------
	section("Configuring MySQL...");
	print "Creating cloudcoder user...\n";
	Run("mysql", "--user=root", "--pass=$ccMysqlRootPasswd",
		"--execute=create user 'cloudcoder'\@'localhost' identified by '$ccMysqlCCPasswd'");
	print "Granting permissions on cloudcoderdb to cloudcoder...\n";
	Run("mysql", "--user=root", "--pass=$ccMysqlRootPasswd",
		"--execute=grant all on cloudcoderdb.* to 'cloudcoder'\@'localhost'");
	
	# ----------------------------------------------------------------------
	# Create cloud user
	# ----------------------------------------------------------------------
	section("Creating cloud user account...");
	RunAdmin(
		cmd => [ 'adduser', '--disabled-password', '--home', '/home/cloud', '--gecos', '', 'cloud' ]
	);

	# ----------------------------------------------------------------------
	# Configure apache2
	# ----------------------------------------------------------------------
	section("Configuring apache2...");
	print "Generating SSL configuration...\n";
	EditApache2DefaultSsl($ccHostname);
	print "Enabling modules...\n";
	RunAdmin(cmd => ['a2enmod', 'proxy']);
	RunAdmin(cmd => ['a2enmod', 'proxy_http']);
	RunAdmin(cmd => ['a2enmod', 'ssl']);
	print "Restarting...\n";
	RunAdmin(cmd => ['service', 'apache2', 'restart']);

	# ----------------------------------------------------------------------
	# Continue as the cloud user to download and configure
	# webapp and builder jarfiles.
	# ----------------------------------------------------------------------
	section("Continuing as cloud user...");
	Run("cp", $program, "/tmp/bootstrap.pl");
	Run("chmod", "a+x", "/tmp/bootstrap.pl");
	RunAdmin(
		asUser => 'cloud',
		cmd => ["/tmp/bootstrap.pl", "step2",
			"ccUser=$ccUser,ccPassword=$ccPasswd,ccFirstName=$ccFirstName," .
			"ccLastName=$ccLastName,ccEmail=$ccEmail,ccWebsite=$ccWebsite," .
			"ccInstitutionName=$ccInstitutionName," .
			"ccMysqlCCPasswd=$ccMysqlCCPasswd,ccHostname=$ccHostname"]);

	# ----------------------------------------------------------------------
	# Copy the configured builder jarfile into the home directory of the current user.
	# ----------------------------------------------------------------------
	my $version = GetLatestVersion();
	my $home = $ENV{'HOME'};
	my $user = $ENV{'USER'};
	print "Copying configured builder jarfile into $home...\n";
	RunAdmin("cp", "/home/cloud/cloudcoderBuilder-$version.jar", $home);
	RunAdmin("chown", $user, "cloudcoderBuilder-$version.jar");

	# ----------------------------------------------------------------------
	# We're done!
	# ----------------------------------------------------------------------
	section("CloudCoder installation successful!");
	my $builderJar = "cloudcoderBuilder-$version.jar";
	print <<"SUCCESS";
It looks like CloudCoder was installed successfully.

You should be able to test your new installation by opening the
following web page:

  https://$ccHostname/cloudcoder

Note that no builders are running, so you won't be able to
test submissions yet.  The builder jar file ($builderJar)
is in the /home/$user directory: you will need to copy
it to the server(s) which will be responsible for building
and testing submissions.
SUCCESS
}

sub Step2 {
	# Complete the installation running as the cloud user
	my $whoami = `whoami`;
	chomp $whoami;
	print "Step2: running as $whoami\n";
	chdir "/home/cloud" || die "Couldn't change directory to /home/cloud: $!\n";

	# Get configuration properties passed from start step
	my %props = split(/,|=/, $ARGV[0]);
	foreach my $name (keys %props) {
		print "$name=$props{$name}\n";
	}

	# Create webapp directory and change to it
	Run("mkdir", "-p", "webapp");
	chdir "webapp" || die "Couldn't change directory to webapp directory: $!\n";

	# ----------------------------------------------------------------------
	# Download webapp and builder distribution jarfiles
	# ----------------------------------------------------------------------

	# Find out what the most recent release version is
	my $version = GetLatestVersion();
	my $appJar = "cloudcoderApp-$version.jar";
	my $builderJar = "cloudcoderBuilder-$version.jar";

	# Download webapp and builder release jarfiles
	section("Downloading $appJar and $builderJar...");
	Run("wget", "$DOWNLOAD_SITE/$appJar");
	Run("wget", "$DOWNLOAD_SITE/$builderJar");

	# ----------------------------------------------------------------------
	# Configure webapp distribution jarfile with
	# generated cloudcoder.properties and keystore
	# ----------------------------------------------------------------------
	section("Configuring $appJar and $builderJar...");

	# Generate cloudcoder.properties
	print "Creating cloudcoder.properties...\n";
	my $pfh = new FileHandle(">cloudcoder.properties");
	my $ccMysqlCCPasswd = $props{ccMysqlCCPasswd};
	print $pfh <<"ENDPROPERTIES";
cloudcoder.db.user=cloudcoder
cloudcoder.db.passwd=$ccMysqlCCPasswd
cloudcoder.db.databaseName=cloudcoderdb
cloudcoder.db.host=localhost
cloudcoder.db.portStr=
cloudcoder.login.service=database
cloudcoder.submitsvc.oop.host=localhost
cloudcoder.submitsvc.oop.numThreads=2
cloudcoder.submitsvc.oop.port=47374
cloudcoder.submitsvc.ssl.cn=None
cloudcoder.submitsvc.ssl.keystore=keystore.jks
cloudcoder.submitsvc.ssl.keystore.password=changeit
cloudcoder.webserver.port=8081
cloudcoder.webserver.contextpath=/cloudcoder
cloudcoder.webserver.localhostonly=true
ENDPROPERTIES
	$pfh->close();

	# Create a keystore
	print "Creating a keystore for communication between webapp and builder...\n";
	Run('keytool', '-genkey', '-noprompt',
		'-alias', 'cloudcoder',
		'-storepass', 'changeit',
		'-keystore', 'keystore.jks',
		'-validity', '3600',
		'-keypass', 'changeit',
		'-dname', "CN=None, OU=None, L=None, ST=None, C=None");

	# Configure webapp jarfile to use the generated cloudcoder.properties
	# and keystore
	print "Configuring $appJar...\n";
	Run("java", "-jar", $appJar, "configure",
		"--editJar=$appJar",
		"--replace=cloudcoder.properties=cloudcoder.properties",
		"--replace=war/WEB-INF/classes/keystore.jks=keystore.jks");

	# Configure builder jarfile to use the same cloudcoder.properties
	# and keystore
	print "Configuring $builderJar...\n";
	Run("java", "-jar", $builderJar, "configure",
		"--editJar=$builderJar",
		"--replace=cloudcoder.properties=cloudcoder.properties",
		"--replace=keystore.jks=keystore.jks");

	Run('rm', '-f', 'cloudcoder.properties', 'keystore.jks');

	# ----------------------------------------------------------------------
	# Create the cloudcoderdb database
	# ----------------------------------------------------------------------
	section("Creating cloudcoderdb database...");
	Run("java", "-jar", $appJar, "createdb", "--props=$ARGV[0],ccRepoUrl=https://cloudcoder.org/repo");

	# ----------------------------------------------------------------------
	# Start the webapp!
	# ----------------------------------------------------------------------
	section("Starting the CloudCoder web application");
	Run("java", "-jar", $appJar, "start");
	
}

sub ask {
	my ($question, $defval) = @_;

	print "$question\n";
	if (defined $defval) {
		print "[default: $defval] ";
	}
	print "==> ";

	my $value = <STDIN>;
	chomp $value;

	if ((defined $defval) && $value =~ /^\s*$/) {
		$value = $defval;
	}

	return $value;
}

sub section {
	my ($name) = @_;
	print "\n";
	print "#" x 72, "\n";
	print " >>> $name <<<\n";
	print "#" x 72, "\n\n";
}

sub RunAdmin {
	my %params = @_;
	die "RunAdmin with no command\n" if (! exists $params{'cmd'});

	# Set environment variables (saving previous values)
	my %origEnv = ();
	if (exists $params{'env'}) {
		foreach my $var (keys %{$params{'env'}}) {
			my $val = $params{'env'}->{$var};
			$origEnv{$var} = $val;
			$ENV{$var} = $val;
		}
	}

	my @sudo = ('sudo', '-p', 'sudo password>> ');
	my @cmd;
	my $asUser = exists $params{'asUser'};
	if ($asUser) {
		@cmd = (@sudo, '-u', $params{'asUser'}, @{$params{'cmd'}});
	} else {
		@cmd = (@sudo, @{$params{'cmd'}});
	}

	my $result;
	if ($dryRun) {
		print "cmd: ", join(' ', @cmd), "\n";
		$result = 1;
	} else {
		$result = system(@cmd)/256 == 0;
	}

	# Restore previous values
	foreach my $var (keys %origEnv) {
		$ENV{$var} = $origEnv{$var};
	}

	if (!$result) {
		my $prog = $cmd[$asUser ? 5 : 3];
		die "Admin command $prog failed\n";
	}
}

sub Run {
	if ($dryRun) {
		print "cmd: ", join(' ', @_), "\n";
	} else {
		system(@_)/256 == 0 || die "Command $_[0] failed\n";
	}
}

sub FindMysqlVersion {
	my $fh = new FileHandle("apt-cache search mysql-server|");
	my $version;
	while (<$fh>) {
		chomp;
		if (/^mysql-server-(\d+(\.\d+)*)\s/) {
			$version = $1;
			last;
		}
	}
	$fh->close();

	die "Couldn't not find mysql version\n" if (!defined $version);
	return $version;
}

sub DebconfSetSelections {
	my ($package, $prop, $value) = @_;
	my $cmd = "echo '$package $prop $value' | sudo -p 'sudo password>> ' debconf-set-selections";
	if ($dryRun) {
		print "cmd: $cmd\n";
	} else {
		system($cmd)/256 == 0 || die "Couldn't run debconf-set-selections\n";
	}
}

sub EditApache2DefaultSsl {
	my ($ccHostname) = @_;

	# Edit /etc/apache2/sites-available/default-ssl to add hostname
	# and transparent proxy support for CloudCoder webapp
	my $in = new FileHandle("</etc/apache2/sites-available/default-ssl");
	my $out = new FileHandle(">/tmp/default-ssl-modified");

	my $alreadyModified = 0;
	my $modCount = 0;

	while (<$in>) {
		chomp;
		print $out "$_\n";
		if (/^\s*<VirtualHost/) {
			print $out <<"ENDSERVERNAME";
	# Modified by CloudCoder bootstrap.pl
	ServerName $ccHostname
ENDSERVERNAME
			$modCount++;
		} elsif (/^\s*ServerAdmin/) {
			print $out <<"ENDPROXY";
	
	# Transparently proxy requests for /cloudcoder to the
	# CloudCoder Jetty server
	ProxyPass /cloudcoder http://localhost:8081/cloudcoder
	ProxyPassReverse /cloudcoder http://localhost:8081/cloudcoder
	<Proxy http://localhost:8081/cloudcoder>
		Order Allow,Deny
		Allow from all
	</Proxy>
ENDPROXY
			$modCount++;
		} elsif (/^\s*# Modified by CloudCoder/) {
			$alreadyModified = 1;
		}
	}
	$in->close();
	$out->close();

	if ($alreadyModified) {
		print "/etc/apache2/sites-available/default-ssl Already modified?\n";
		return;
	}

	if ($modCount != 2) {
		die "/etc/apache2/sites-available/default-ssl is not in expected format\n";
	}

	RunAdmin(cmd => ['cp', '/tmp/default-ssl-modified',
		'/etc/apache2/sites-available/cloudcoder-ssl']);
	RunAdmin(cmd => ['ln', '-s', '/etc/apache2/sites-available/cloudcoder-ssl', '/etc/apache2/sites-enabled/cloudcoder-ssl']);
}

sub GetLatestVersion {
	my $fh = new FileHandle("wget --quiet --output-document=- $DOWNLOAD_SITE/LATEST|");
	my $version;
	while (<$fh>) {
		if (/^\s*(\d+(\.\d+)*)\s*$/ || /^\s*v(\d+(\.\d+)*)\s*$/) {
			$version = $1;
			last;
		}
	}
	$fh->close();
	die "Could not determine latest CloudCoder release version\n" if (!defined $version);
	return $version;
}

# vim:ts=2:
