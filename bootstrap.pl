#! /usr/bin/perl -w

use strict;
use FileHandle;

# Bootstrap CloudCoder on an Ubuntu server

my $program = $0;
#print "program=$program\n";
#exit 0;

my $dryRun = 1;

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
enter.
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
	
	# ----------------------------------------------------------------------
	# Install/configure required packages
	# ----------------------------------------------------------------------
	section("Installing required packages...");
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

	# Continue as the cloud user to complete the installation
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
}

sub Step2 {
	# Complete the installation running as the cloud user
	my $whoami = `whoami`;
	chomp $whoami;
	print "Step2: running as $whoami\n";
	chdir "/home/cloud" || die "Couldn't change directory to /home/cloud: $!\n";

	# Get configuration properties passed from start step
	my %props = split(/,|=/, @ARGV[0]);
	foreach my $name (keys %props) {
		print "$name=$props{$name}\n";
	}

	# Create webapp directory and change to it
	Run("mkdir", "-p", "webapp");
	chdir "webapp" || die "Couldn't change directory to webapp directory: $!\n";

	# ----------------------------------------------------------------------
	# Download webapp distribution jarfile
	# ----------------------------------------------------------------------
	# TODO: automatically determine latest version
	my $appJar = "cloudcoderApp-v0.0.1.jar";
	section("Downloading $appJar...");
	my $appUrl = "https://s3.amazonaws.com/cloudcoder-binaries/$appJar";
	Run("wget", $appUrl);

	# ----------------------------------------------------------------------
	# Configure webapp distribution jarfile
	# ----------------------------------------------------------------------
	section("Configuring $appJar...");
	# Generate cloudcoder.properties
	print "Creating cloudcoder.properties...\n";
	my $pfh = new FileHandle(">cloudcoder.properties");
	print $pfh <<"ENDPROPERTIES";
cloudcoder.db.user=cloudcoder
cloudcoder.db.passwd=$props{ccMysqlCCPasswd}
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

	# Configure webapp jarfile to use the generated cloudcoder.properties
	print "Configuring $appJar...\n";
	Run("java", "-jar", $appJar, "configure",
		"--useProperties=cloudcoder.properties",
		"--editJar=$appJar",
		"--noBuilder");

	# ----------------------------------------------------------------------
	# Create the cloudcoderdb database
	# ----------------------------------------------------------------------
	section("Creating cloudcoderdb database...");
	# TODO

	# At this point, it should be possible to start the webapp!
	
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
	if (exists $params{'asUser'}) {
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

	die "Admin command $cmd[3] failed\n" if (!$result);
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
			print $out <<"SERVERNAME";
	# Modified by CloudCoder bootstrap.pl
	ServerName $ccHostname
SERVERNAME
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

# vim:ts=2:
