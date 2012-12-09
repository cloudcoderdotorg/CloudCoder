#! /usr/bin/perl -w

use strict;
use FileHandle;

# Script to collect all configuration information needed to
# build and deploy the CloudCoder webapp and Builder.
# Generates a cloudcoder.properties file.

my @propertyNames = ();
my %properties = ();
my %prior = ();
my $configRepoWebapp = 0;
my $useDefaultKeystore = 0;

if (scalar(@ARGV) >= 1) {
	# If the "--repo" command-line argument is specified, then
	# include configuration for the repository webapp (and its
	# webserver).
	my $arg = shift @ARGV;
	if ($arg eq '--repo') {
		$configRepoWebapp = 1;
		print "Configuring for CloudCoder exercise repository\n\n";
	} else {
		die "Unknown option: $arg\n";
	}
}

print "Welcome to the CloudCoder configuration script!\n";
print "Please enter the information needed to configure CloudCoder for your system.\n";
print "You can just hit enter to accept the default value (if there is one).\n";
print "\n";

if (-r "cloudcoder.properties") {
	my $reload = ask("You seem to have a cloudcoder.properties file already: use it to\n" .
		"load defaults?", "yes");

	if ((lc $reload) eq 'yes') {
		my $prior_fh = new FileHandle("<cloudcoder.properties") || die;
		while (<$prior_fh>) {
			chomp;
			if (/^([^=]+)=(.*)$/) {
				$prior{$1} = $2;
			}
		}
		$prior_fh->close();
	}
}

askprop("Where is your GWT SDK installed (the directory with webAppCreator in it)?",
	"gwt.sdk", undef);

section("Database configuration properties");

askprop("What MySQL username will the webapp use to connect to the database?",
	"cloudcoder.db.user", undef);
askprop("What MySQL password will the webapp use to connect to the database?",
	"cloudcoder.db.passwd", undef);
askprop("What MySQL database will contain the CloudCoder tables?",
	"cloudcoder.db.databaseName", "cloudcoderdb");
askprop("What host will CloudCoder connect to to access the MySQL database?",
	"cloudcoder.db.host", "localhost");
askprop("If MySQL is running on a non-standard port, enter :XXXX (e.g, :8889 for MAMP).\n" .
	"Just hit enter if MySQL is running on the standard port.",
	"cloudcoder.db.portStr", undef);

section("Login service properties");

askprop("Which login service do you want to use (imap or database)?",
	"cloudcoder.login.service", "database");
if ($properties{"cloudcoder.login.service"} eq 'imap') {
	askprop("What is the hostname or IP address of your IMAP server?",
		"cloudcoder.login.host", undef);
}

section("Builder properties");

askprop("What host will the CloudCoder webapp be running on?\n" .
	"(This information is needed by the Builder so it knows how to connect\n" .
	"to the webapp.)",
	"cloudcoder.submitsvc.oop.host", "localhost");
askprop("How many threads should the Builder use? (suggestion: 1 per core)",
	"cloudcoder.submitsvc.oop.numThreads", "2");
askprop("What port will the CloudCoder webapp use to listen for connections from\n" .
	"Builders?",
	"cloudcoder.submitsvc.oop.port", "47374");

section("TLS/SSL (secure communication between webapp and builder(s)");

my $useDefaultKeystoreYN = ask("Do you want to use the default keystore\n" .
	"(Answer 'yes' for development, 'no' for production)", "no");
print "\n";
$useDefaultKeystore = (lc $useDefaultKeystoreYN) eq 'yes';

if ($useDefaultKeystore) {
	setprop("cloudcoder.submitsvc.ssl.cn", 'None');
	setprop("cloudcoder.submitsvc.ssl.keystore", 'keystore.jks');
	setprop("cloudcoder.submitsvc.ssl.keystore.password", 'changeit');
} else {
	askprop("What is the hostname of your institution?",
		  "cloudcoder.submitsvc.ssl.cn", "None");
	askprop("What is the name of the keystore that will store your public/private keypair?\n" .
		"(A new keystore will be created if it doesn't already exist)",
		"cloudcoder.submitsvc.ssl.keystore", "keystore.jks");
	askprop("What is the keystore/key password?",
		"cloudcoder.submitsvc.ssl.keystore.password", "changeit");
}

section("Web server properties (webapp)");

askprop("What port will the CloudCoder web server listen on?",
	"cloudcoder.webserver.port", "8081");
askprop("What context path should the webapp use?",
	"cloudcoder.webserver.contextpath", "/cloudcoder");
askprop("Should the CloudCoder web server accept connections only from localhost?\n" .
	"(Set this to 'true' if using a reverse proxy, which is recommended)",
	"cloudcoder.webserver.localhostonly", "true");

if ($configRepoWebapp) {
	section("Database configuration (repository webapp)");

	askprop("What MySQL username will the repository webapp use to connect to the database?",
		"cloudcoder.repoapp.db.user", undef);
	askprop("What MySQL password will the repository webapp use to connect to the database?",
		"cloudcoder.repoapp.db.passwd", undef);
	askprop("What MySQL database will contain the repository tables?",
		"cloudcoder.repoapp.db.databaseName", "cloudcoderrepodb");
	askprop("What host will the repository webapp connect to to access the MySQL database?",
		"cloudcoder.repoapp.db.host", "localhost");
	askprop("If MySQL is running on a non-standard port, enter :XXXX (e.g, :8889 for MAMP).\n" .
		"Just hit enter if MySQL is running on the standard port.",
		"cloudcoder.repoapp.db.portStr", undef);

	section("Webserver configuration (repository webapp)");

	askprop("What port will the exercise repository web server listen on?",
		"cloudcoder.repoapp.webserver.port", "8082");
	askprop("What context path should the exercise repository webapp use?",
		"cloudcoder.repoapp.webserver.contextpath", "/repo");
	askprop("Should the exercise repository web server accept connections only from localhost?\n" .
		"(Set this to 'true' if using a reverse proxy, which is recommended)",
		"cloudcoder.repoapp.webserver.localhostonly", "true");
	askprop("What SMTP server should the repo webapp use to send mail?",
		"cloudcoder.repoapp.smtp.host", "smtp.1and1.com");
	askprop("What SMTP port should the repo webapp use to send mail?",
		"cloudcoder.repoapp.smtp.port", "587");
	askprop("What SMTP username should the repo webapp use?",
		"cloudcoder.repoapp.smtp.user", undef);
	askprop("What SMTP password should the repo webapp use?",
		"cloudcoder.repoapp.smtp.passwd", undef);
}

my $confirm = ask("Write configuration file (cloudcoder.properties)?", "yes");
if ((lc $confirm) ne 'yes') {
	print "Properties not written\n";
	exit 1;
}

print "Writing properties...";
STDOUT->flush();
my $fh = new FileHandle(">cloudcoder.properties");
foreach my $property (@propertyNames) {
	print $fh "$property=$properties{$property}\n";
}
$fh->close();
print "Done!\n";

if ($useDefaultKeystore) {
	# Use the default keystore.
	run("cp CloudCoderBuilder2/src/defaultkeystore.jks keystore.jks");
} else {
	# Create a keystore if it doesn't already exist.
	if (! -e $properties{'cloudcoder.submitsvc.ssl.keystore'}) {
		run('./createKeystore.pl',
			$properties{'cloudcoder.submitsvc.ssl.keystore'},
			$properties{'cloudcoder.submitsvc.ssl.keystore.password'},
			$properties{'cloudcoder.submitsvc.ssl.cn'});
	}
}

sub section {
	my ($name) = @_;
	print "#" x 72, "\n";
	print " >>> $name <<<\n";
	print "#" x 72, "\n\n";
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

sub askprop {
	my ($question, $property, $defval) = @_;

	# If there are prior properties, use them as defaults.
	if (exists $prior{$property}) {
		$defval = $prior{$property};
	}

	my $value = ask($question, $defval);

	setprop($property, $value);

	print "\n";
}

sub setprop {
	my ($property, $value) = @_;
	push @propertyNames, $property;
	$properties{$property} = $value;
}

sub run {
	system(@_)/256 == 0 || die "Command $_[0] failed\n";
}
