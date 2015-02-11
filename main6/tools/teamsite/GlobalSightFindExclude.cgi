#!../../iw-perl/bin/iwperl

###########################################################
# GLOBAL SIGHT/INTERWOVEN INTEGRATION SCRIPT
#
# This script is called to return directories with
# specified exclusion to be used as basis for delivering
# information about the TeamSite directory structure
#
###########################################################

use CGI;

# Output for webserver
print "Content-type: text/html\n\n";

# Get CGI object
my $query = new CGI;

# AutoFlush the buffer
$| = 1;

## Bring in directory structure information
my $startingPath = $query->param("StartingPath");
my $ref_tlign = $query->param("IgnoreTopLevel");
my $ref_ign = $query->param("IgnoreDirectories");
my $ignore_dirs = $query->param("TerminalDir");
my $separator = $query->param("Separator");

my @dirents;

# form the array arguments for find_excluded
my @array_tlign = split($separator, $ref_tlign);
my @array_ign = split($separator, $ref_ign);

@dirents = find_excluded( $startingPath, 
				\@array_tlign, \@array_ign, $ignore_dirs);

# send a blank line before sending the content of the response
print "\n";

# form string from array for transmission
foreach my $kentry (@dirents) {
	print "$kentry";
	print "$separator";
}

exit;

# Taken directly from .../cap/bin/as/mod/Util.pm
# a function to descend a directory, excluding some subdirs
# $_[0]: directory to descend
# $_[1]: reference to a list of ignored top-level names
# $_[2]: reference to a list of ignored directories (period)
# $_[3]: if non-zero and defined, don't include terminal dirs
sub find_excluded {
    my $root = shift;
    my $ref_tlign = shift;
    my $ref_ign = shift;
    my $ignore_dirs = shift;
    my %ignored = ();
    if(!$root)
    {
        return ();
    }
    map {$ignored{$_} = 1} @$ref_tlign if ref $ref_tlign;
    map {$ignored{$_} = 1} @$ref_ign if ref $ref_ign;
    my @rv = ();
    my @dirs = ();
    opendir DIR, $root or return ();
    @dirs = readdir DIR;
    closedir DIR;
    for (@dirs)
    {
        next if $_ eq '.' or $_ eq '..';
        next if $ignored{$_};
        next unless -f "$root/$_" or -d _;
        if(!-f _)   # must be directory or symlink
        {
            my $name = $_;
            my @newdirs = find_excluded("$root/$_", 0, $ref_ign, $ignore_dirs);

            if (@newdirs) {
                for (@newdirs)
                {
                    # add the directory and filename
                    push @rv, "$name/$_";
                }
            }
            else {
                # add the empty directory
                push @rv, $name."/IWdummy" if $ignore_dirs;
            }
        }
        elsif(-r _)
        {
            push @rv, $_;
        }
    }
    return @rv;
}
