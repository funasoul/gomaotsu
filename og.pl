#!/usr/bin/env perl
#
# Last modified: Mon, 26 Jun 2017 17:34:45 +0900
#
# Requirement: This script requires Unicode::GCString to be installed
# on your system..
# (ex.) sudo port install p5-unicode-linebreak
#
use strict;
use warnings;

our $VERSION = '1.0.0';

# Otome File (Change this!!)
my $otomelist = "/Users/funa/git/gomaotsu/OtomeList.csv";
my $friendlist = "/Users/funa/git/gomaotsu/FriendList.csv";

# Getopt
use Getopt::Long qw(:config posix_default no_ignore_case gnu_compat);

# utf-8 settings
use Unicode::GCString;
use utf8;
binmode STDIN,  ":utf8";
binmode STDOUT, ":utf8";
use I18N::Langinfo qw(langinfo CODESET);
use Encode qw(decode);
my $codeset = langinfo(CODESET);
@ARGV = map { decode $codeset, $_ } @ARGV;

# Display settings
my $red     = "\x1b[1;31m";
my $green   = "\x1b[1;32m";
my $yellow  = "\x1b[1;33m";
my $blue    = "\x1b[1;34m";
my $magenta = "\x1b[1;35m";
my $cyan    = "\x1b[1;36m";
my $default = "\x1b[0m";

my %otomes;
(my $myname= $0) =~ s,.*[/\\],,;

my $usage = <<_eou_;
Otome Grep script version $VERSION.
This script will grep through your OtomeList.csv and prints out the results.
Usage:  $myname [-hmv] [-c otomename] [-p otomename] [keyword1 keyword2 ...]
            -h --help      ... Show this message
            -i --id        ... Print Id.
            -m --mpsort    ... Sort by MP
            -v --verbose   ... Print Otome even if you don't have it
            -c, --childof  ... Print child of specified Otome
            -p, --parentof ... Print parent of specified Otome
    If no argument is set, it prints all Otome you have.
_eou_
my $usage_long = $usage . <<_eol_;
    (ex.) $myname 闇 ニードル
          $myname -c カトレア
_eol_

# Parse options
my $opt_help = 0;
my $opt_sortbyMP = 0;
my $opt_printId = 0;
my $opt_verbose = 0;
my $opt_child_of = "";
my $opt_parent_of = "";

GetOptions('help|h' => \$opt_help, 'mpsort|m' => \$opt_sortbyMP, 'id|i' => \$opt_printId, 'verbose|v' => \$opt_verbose, 'childof|c=s' => \$opt_child_of, 'parentof|p=s' => \$opt_parent_of) || die($usage);
if ($opt_help) {
  print $usage_long;
  exit;
}

# read CSV files
read_otomelist();
read_friendlist();

# here we go.
if (isFindParent()) { # find parent
  foreach my $id (keys(%otomes)) {
    if ($otomes{$id}->{name} =~ /$opt_parent_of/) {
      mark_parents_of($id);
    }
  }
} elsif (isFindChild()) { # find child
  foreach my $id (keys(%otomes)) {
    if ($otomes{$id}->{name} =~ /$opt_child_of/) {
      mark_children_of($id);
    }
  }
} else { # at first, mark only owned otome (if opt_verbose is set, mark all)
  foreach my $id (keys(%otomes)) {
    if ($opt_verbose) {
      $otomes{$id}->{match} = 1;
    } else {
      $otomes{$id}->{match} = $otomes{$id}->{own};
    }
  }
}

# grep like function
foreach my $id (keys(%otomes)) {
  foreach my $arg (@ARGV) {
    # if $arg is 1, 2, 3, 4, 5, 6, then match with hoshi only.
    if (isInt($arg) == 1 and $arg > 0 and $arg < 7) {
      if ($otomes{$id}->{hoshi} ne $arg) {
        $otomes{$id}->{match} = 0;
        last;
      } else {
        next;
      }
    }
    $arg = "火" if $arg eq "炎";
    $arg = "火" if $arg eq "赤";
    $arg = "水" if $arg eq "青";
    $arg = "風" if $arg eq "緑";
    $arg = "光" if $arg eq "白";
    $arg = "闇" if $arg eq "紫";
    $arg = "闇" if $arg eq "黒";
    # if $arg is 火,水,風,光,闇 then match with zokusei only.
    if ($arg eq "火" or $arg eq "水" or $arg eq "風" or $arg eq "光" or $arg eq "闇") {
      if ($otomes{$id}->{zokusei} ne $arg) {
        $otomes{$id}->{match} = 0;
        last;
      } else {
        next;
      }
    }
    # if $arg is 未,満 then match with love only.
    if ($arg eq "未" or $arg eq "満") {
      if (($arg eq "未" and $otomes{$id}->{love} != 0) or ($arg eq "満" and $otomes{$id}->{love} != 1)) {
        $otomes{$id}->{match} = 0;
        last;
      } else {
        next;
      }
    }
    # case insensitive match.
    if (toString($id) !~ /$arg/i) {
      $otomes{$id}->{match} = 0;
      last;
    }
  }
}

# print marked Otomes.
if ($opt_sortbyMP == 1) {
  foreach my $id (sort{ $otomes{$b}->{mp} <=> $otomes{$a}->{mp} or
    $otomes{$a}->{zokusei} cmp $otomes{$b}->{zokusei} or
    $otomes{$b}->{hoshi} <=> $otomes{$a}->{hoshi} or
    $otomes{$b}->{bunrui} cmp $otomes{$a}->{bunrui} or
    $otomes{$b}->{shot} cmp $otomes{$a}->{shot}
    } keys(%otomes)) {
    if ($otomes{$id}->{match} == 1) {
      print_line($id);
    }
  }
} else {
  # Arghhh.... yuck.
  foreach my $id (sort{ $otomes{$a}->{zokusei} cmp $otomes{$b}->{zokusei} or
    $otomes{$b}->{hoshi} <=> $otomes{$a}->{hoshi} or
    $otomes{$b}->{bunrui} cmp $otomes{$a}->{bunrui} or
    $otomes{$b}->{shot} cmp $otomes{$a}->{shot} or
    $otomes{$b}->{mp} <=> $otomes{$a}->{mp}
    } keys(%otomes)) {
    if ($otomes{$id}->{match} == 1) {
      print_line($id);
    }
  }
}

## Functions
sub mark_children_of {
  my $id = shift;
  # mark all children of $otomes{$id}
  foreach my $child (keys(%otomes)) {
    next if (!$otomes{$child}->{own} && !$opt_verbose);
    foreach my $fname (@{$otomes{$child}->{flist}}) {
      if ($otomes{$id}->{name} eq $fname) { # if $id is a parent of $child
        $otomes{$child}->{match} = 1;
      }
    }
  }
}

sub mark_parents_of {
  my $id = shift;
  # mark all parent of $otomes{$id}
  foreach my $fname (@{$otomes{$id}->{flist}}) { 
    mark_otome_by_name($fname);
  }
}

sub mark_otome_by_name {
  my $name = shift;
  foreach my $id (keys(%otomes)) {
    next if (!$otomes{$id}->{own} && !$opt_verbose);
    if ($otomes{$id}->{name} eq $name) {
      $otomes{$id}->{match} = 1;
      last;
    }
  }
}

sub read_otomelist {
  open(IN, "<:utf8", $otomelist) or die "Cannot open file\n";
  while(<IN>){
    chomp;
    my @array = (split(","));
    # CSV format:
    #  0,  1, 2  ,  3   ,  4   , 5  , 6, 7  ,     8      ,     9    ,   10   ,    11    , 12 ,  13
    # #No.,星,属性,使い魔,コスト,魔力,HP,分類,ショット種類,スキル種類,スキル名,スキル効果,所持,親密度max
    my $id      = $array[0];
    next if ($id =~ /^#/);
    $otomes{$id}->{hoshi}      = $array[1];
    $otomes{$id}->{zokusei}    = $array[2];
    $otomes{$id}->{name}       = conv_zenkaku($array[3]);
    $otomes{$id}->{cost}       = $array[4];
    $otomes{$id}->{mp}         = $array[5];
    $otomes{$id}->{hp}         = $array[6];
    $otomes{$id}->{bunrui}     = $array[7];
    $otomes{$id}->{shot}       = $array[8];
    $otomes{$id}->{skill}      = conv_zenkaku($array[9]);
    $otomes{$id}->{skillname}  = $array[10];
    $otomes{$id}->{skillkouka} = conv_zenkaku($array[11]);
    $otomes{$id}->{own}        = $array[12];
    $otomes{$id}->{love}       = $array[13];
    $otomes{$id}->{match}      = 0; # initilaize
  }
  close(IN);
}

sub read_friendlist {
  open(FIN, "<:utf8", $friendlist) or die "Cannot open file\n";
  while(<FIN>) {
    chomp;
    my $line = conv_zenkaku($_);
    my ($id, @flist) = (split(",", $line));
    $otomes{$id}->{flist}   = \@flist;
  }
  close(FIN);
}

sub print_line {
  my $id = shift;
  print_id($id) if ($opt_printId);
  print_star($id);
  print_mp($id);
  print_bunrui($id);
  print_shot($id);
  print_name($id);
  print_skill($id);
  print_love($id);
  print "\n";
}

sub print_color {
  my ($str, $color) = @_;
  printf("%s$str%s", $color, $default);
}

sub print_id {
  my $id = shift;
  my $color = get_color_byid($id);
  my $str = sprintf("%4s ", $id);
  print_color($str, $color);
  return length($str);
}

sub print_star {
  my $id = shift;
  my $str = sprintf("★ %s ", $otomes{$id}->{hoshi});
  print_color($str, get_color_byid($id));
  return length($str);
}

sub print_mp {
  my $id = shift;
  my $str = sprintf("%3s ", $otomes{$id}->{mp});
  printf($str);
  return length($str);
}

sub print_bunrui {
  my $id = shift;
  my $str = sprintf("%s ", $otomes{$id}->{bunrui});
  print_color($str, get_color_bybunrui($otomes{$id}->{bunrui}));
  return length($str);
}

sub print_string {
  my ($str, $deflen) = @_;
  my $gcstring = Unicode::GCString->new($str);
  my $colwidth = $gcstring->columns();
  if ($colwidth > $deflen) {
    print $gcstring->substr(0,$deflen);
  } else {
    print $gcstring;
    print " " x ($deflen - $colwidth);
  }
  print " ";
}

sub print_shot {
  my $id = shift;
  print_string($otomes{$id}->{shot}, 16);
}

sub print_skill {
  my $id = shift;
  print_string($otomes{$id}->{skill}, 20);
}

sub print_love {
  my $id = shift;
  if ($otomes{$id}->{love} == 1) {
    print_color("満", $red);
  } else {
    if ($otomes{$id}->{own} ==1) {
      print_color("未", $cyan);
    } else {
      print_color("未", $yellow);
    }
  }
}

sub print_name {
  my $id = shift;
  print_string($otomes{$id}->{name}, 22);
}

sub toString {
  my $id = shift;
  my $str = $id . ",";
  #  0,  1, 2  ,  3   ,  4   , 5  , 6, 7  ,     8      ,     9    ,   10   ,    11    , 12 ,  13
  # #No.,星,属性,使い魔,コスト,魔力,HP,分類,ショット種類,スキル種類,スキル名,スキル効果,所持,親密度max
  $str .= $otomes{$id}->{hoshi} . ",";
  $str .= $otomes{$id}->{zokusei} . ",";
  $str .= $otomes{$id}->{name} . ",";
  $str .= $otomes{$id}->{mp} . ",";
  $str .= $otomes{$id}->{hp} . ",";
  $str .= $otomes{$id}->{bunrui} . ",";
  $str .= $otomes{$id}->{shot} . ",";
  $str .= $otomes{$id}->{skill} . ",";
  $str .= $otomes{$id}->{skillkouka};
  return $str;
}

sub conv_zenkaku {
  my $str = shift;
  $str =~ tr/０-９Ａ-Ｚａ-ｚ/0-9A-Za-z/; # I hate zenkaku chars
  $str =~ s/【/[/g;   # I hate zenkaku brackets
  $str =~ s/】/] /g;  # I hate zenkaku brackets
  $str =~ s/（/(/g;   # I hate zenkaku brackets
  $str =~ s/）/)/g;   # I hate zenkaku brackets
  $str =~ s/\(/ (/g;
  $str =~ s/\s+\(/ (/g;
  return $str;
}

sub get_color {
  my $zokusei = shift;
  my $color = $default;
  if ($zokusei eq "火") {
    $color = $red;
  } elsif ($zokusei eq "水") {
    $color = $cyan;
  } elsif ($zokusei eq "風") {
    $color = $green;
  } elsif ($zokusei eq "闇") {
    $color = $magenta;
  }
  return $color;
}

sub get_color_byid {
  my $id = shift;
  my $zokusei = $otomes{$id}->{zokusei};
  return get_color($zokusei);
}

sub get_color_bybunrui {
  my $bunrui = shift;
  my $color = $default;
  if ($bunrui eq "拡散") {
    $color = $yellow;
  }
  return $color;
}

sub isInt {
  my $num = shift;
  return 1 if ($num =~ /^-?\d+\z/);
  return 0;
}

sub isFindChild {
  return 1 if ($opt_child_of ne "");
  return 0;
}

sub isFindParent {
  return 1 if ($opt_parent_of ne "");
  return 0;
}
