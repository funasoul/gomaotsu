#!/usr/bin/env perl
#
# Last modified: Mon, 15 Aug 2016 05:14:53 +0900
#
# Requirement: This script requires Unicode::GCString to be installed
# on your system..
# (ex.) sudo port install p5-unicode-linebreak
#
use strict;
use warnings;

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
Otome Grep script.
This script will grep through your OtomeList.csv and prints out the results.
Usage:  $myname [-hm] [-c otomename] [-p otomename] [keyword1 keyword2 ...]
            -h --help      ... Show this message
            -m --mpsort    ... Sort by MP
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
my $opt_child_of = "";
my $opt_parent_of = "";

GetOptions('help|h' => \$opt_help, 'mpsort|m' => \$opt_sortbyMP, 'childof|c=s' => \$opt_child_of, 'parentof|p=s' => \$opt_parent_of) || die($usage);
if ($opt_help) {
  print $usage_long;
  exit;
}

# read CSV files
read_otomelist();
read_friendlist();

# 
if (isFindParent() || isFindChild()) {  # find parent or child
  foreach my $id (keys(%otomes)) {
    $otomes{$id}->{match} = 0;
    if (isFindParent()) {
      if ($otomes{$id}->{name} =~ /$opt_parent_of/) {
        $otomes{$id}->{match} = 1;
      }
    }
    # find child
    if (isFindChild()) {
      next;
    }
  }
} else {  # grep like function
  foreach my $id (keys(%otomes)) {
    $otomes{$id}->{match} = $otomes{$id}->{own};
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
}

if ($opt_sortbyMP == 1) {
  foreach my $id (sort{ $otomes{$b}->{'mp'} <=> $otomes{$a}->{'mp'} or
    $otomes{$a}->{'zokusei'} cmp $otomes{$b}->{'zokusei'} or
    $otomes{$b}->{'hoshi'} <=> $otomes{$a}->{'hoshi'} or
    $otomes{$b}->{'bunrui'} cmp $otomes{$a}->{'bunrui'} or
    $otomes{$b}->{'shot'} cmp $otomes{$a}->{'shot'}
    } keys(%otomes)) {
    if ($otomes{$id}->{match} == 1) {
      print_line($id);
    }
  }
} else {
  # Arghhh.... yuck.
  foreach my $id (sort{ $otomes{$a}->{'zokusei'} cmp $otomes{$b}->{'zokusei'} or
    $otomes{$b}->{'hoshi'} <=> $otomes{$a}->{'hoshi'} or
    $otomes{$b}->{'bunrui'} cmp $otomes{$a}->{'bunrui'} or
    $otomes{$b}->{'shot'} cmp $otomes{$a}->{'shot'} or
    $otomes{$b}->{'mp'} <=> $otomes{$a}->{'mp'}
    } keys(%otomes)) {
    if ($otomes{$id}->{match} == 1) {
      print_line($id);
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
    my $hoshi   = $array[1];
    my $zokusei = $array[2];
    my $name    = $array[3];
    $name =~ s/【/[/;   # I hate zenkaku brackets
    $name =~ s/】/] /;  # I hate zenkaku brackets
    my $mp      = $array[5];
    my $hp      = $array[6];
    my $bunrui  = $array[7];
    my $shot    = $array[8];
    my $skill   = $array[9];
    my $own     = $array[12];
    my $love    = $array[13];
    next if ($id =~ /^#/);
    $otomes{$id}->{hoshi}   = $hoshi;
    $otomes{$id}->{zokusei} = $zokusei;
    $otomes{$id}->{name}    = $name;
    $otomes{$id}->{mp}      = $mp;
    $otomes{$id}->{hp}      = $hp;
    $otomes{$id}->{bunrui}  = $bunrui;
    $otomes{$id}->{shot}    = $shot;
    $otomes{$id}->{skill}   = $skill;
    $otomes{$id}->{own}     = $own;
    $otomes{$id}->{love}    = $love;
  }
  close(IN);
}

sub read_friendlist {
  open(FIN, "<:utf8", $friendlist) or die "Cannot open file\n";
  while(<FIN>) {
    chomp;
    my ($id, @farray) = (split(","));
    $otomes{$id}->{farray}   = @farray;
  }
  close(FIN);
}

sub print_line {
  my $id = shift;
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
  my $str = sprintf("%-3s ", $id);
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
    print_color("未", $cyan);
  }
}

sub print_name {
  my $id = shift;
  print_string($otomes{$id}->{name}, 20);
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
  $str .= $otomes{$id}->{skill};
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
