#! /usr/bin/perl -w

use FileHandle;
use strict;

# Fetch external dependencies and copy them to specified locations,
# as specified by a deps file.
#
# The deps file should be a series of entries in the following form:
#
# http://something.com/blah/awesome-lib.jar
#    dir1/awesome-lib.jar
#    dir2/yeah/awesome-lib.jar
#
# Each file to download should be on a line with no leading spaces.
# Each target (file to which the downloaded file should be copied)
# should be on a line with at least one leading space.
#
# The default deps file is "default.deps", but can be overridden with
# the --deps=filename option.

# Handle command line options.
my $depsFile = 'default.deps';
my $mode = 'fetch';
if (scalar(@ARGV) > 0) {
	my $arg = shift @ARGV;
	if ($arg =~ /^--deps=(.*)$/) {
		$depsFile = $1;
	} elsif ($arg eq '--delete') {
		$mode = 'delete';
	} elsif ($arg eq '--list-targets') {
		$mode = 'list';
	} else {
		die "Unknown option: $arg\n";
	}
}

# Read deps file.
my @deps = ReadDeps($depsFile);
#PrintDeps(@deps);
#exit 0;

if ($mode eq 'fetch') {
	FetchAll();
} elsif ($mode eq 'delete') {
	DeleteTargets();
} elsif ($mode eq 'list') {
	ListTargets(@deps);
}

sub DeleteTargets {
	print "Deleting all targets...\n";
	foreach my $dep (@deps) {
		my @targets = @{$dep->[1]};
		foreach my $target (@targets) {
			print "  $target\n";
			Run('rm', '-f', $target);
		}
	}
	print "done\n";
}

sub FetchAll {
	my $downloaded = 0;
	my $copied = 0;
	foreach my $dep (@deps) {
		my $download = $dep->[0];
		my @targets = @{$dep->[1]};
		my @neededTargets = ();
	
		# Check to see which targets are needed.
		foreach my $target (@targets) {
			if (! -e $target) {
				push @neededTargets, $target;
			}
		}
	
		# If there are needed targets...
		if (scalar(@neededTargets) > 0) {
			# Download the jar (if it's not already in the deps directory)
			my $jar = File($download);
			my $t = "deps/$jar";
			if (! -e $t) {
				Download($download, $t);
				$downloaded++;
			}
			# Copy it to all needed targets
			foreach my $neededTarget (@neededTargets) {
				Copy($t, $neededTarget);
				$copied++;
			}
		}
	}
	print "$downloaded file(s) downloaded\n";
	print "$copied file(s) copied\n";
}

sub Download {
	my ($jar, $toFile) = @_;
	EnsureDirExists($toFile);
	my $file = File($jar);
	print "Fetching $jar...\n";
	Run('wget', $jar, "--output-document=$toFile");
}

sub Copy {
	my ($file, $target) = @_;
	print "Copying $file to $target...\n";
	EnsureDirExists($target);
	Run('cp', $file, $target);
}

sub File {
	my ($jar) = @_;
	die if !($jar =~ m,/([^/]+)$,);
	return $1;
}

sub Dir {
	my ($jar) = @_;
	my $dir = ($jar =~ m,^(.*)/[^/]+$,) ? $1 : '';
	return $1;
}

sub Run {
	my @cmd = @_;
	system(@cmd)/256 == 0 || die ("Could not run command: " . join(@cmd, ' ') . "\n");
}

sub ReadDeps {
	my ($filename) = @_;
	my @deps = ();
	my $fh = new FileHandle("<$filename") || die "Couldn't open deps file $filename: $!\n";
	my $download;
	my $targets = [];
	while (<$fh>) {
		chomp;
		if (/^\s*#/) {
			# Comment: ignore
		} elsif (/^(\S+)/) {
			# Download
			if (defined $download) {
				# Push previous download/targets
				push @deps, [ $download, $targets ];
			}
			# Start new download/targets
			$download = $1;
			$targets = [];
		} elsif (/^\s+(.*)/) {
			# Add target to current download
			push @{$targets}, $1;
		}
	}
	push @deps, [ $download, $targets ];
	$fh->close();
	return @deps;
}

sub PrintDeps {
	my @deps = @_;
	foreach my $d (@deps) {
		print "download: ", $d->[0], "\n";
		my @targets = @{$d->[1]};
		foreach my $target (@targets) {
			print "  target: $target\n";
		}
	}
	exit 0;
}

sub ListTargets {
	my @deps = @_;
	foreach my $d (@deps) {
		foreach my $target (@{$d->[1]}) {
			print "$target\n";
		}
	}
}

sub EnsureDirExists {
	my ($toFile) = @_;
	my $dir = Dir($toFile);
	if ($dir ne '' && (! -d $dir)) {
		Run('mkdir', '-p', $dir);
	}
}

# vim:ts=2:
