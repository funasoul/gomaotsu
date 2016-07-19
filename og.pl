#!/usr/bin/env perl
#
# Last modified: Wed, 20 Jul 2016 04:30:48 +0900
#
# Requirement: This script requires Unicode::GCString to be installed
# on your system..
# (ex.) sudo port install p5-unicode-linebreak
#
use strict;
use warnings;

# Otome File (Change this!!)
my $otomelist = "/Users/funa/git/gomaotsu/OtomeList.csv";

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
my $sortbyMP = 0;
if ($ARGV[0] eq "-m" or $ARGV[0] eq "-M") {
  $sortbyMP = 1;
  shift @ARGV;
}
(my $myname= $0) =~ s,.*[/\\],,;
my $usage = <<_eou_;
Otome Grep script.
This script will grep through your OtomeList.csv and prints out the results.
Usage:  $myname [-m] [keyword1 keyword2 ...]
           -m ... Sort by MP
    If no argument is set, it prints all Otome you have.
    (ex.) $myname 闇 ニードル
_eou_

open(IN, "<:utf8", $otomelist) or die "Cannot open file\n";

while(<IN>){
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
  if ($own == 1) {
    my $match = 1;
    foreach my $arg (@ARGV) {
      # if $arg is 1, 2, 3, 4, 5, 6, then match with hoshi only.
      if (isInt($arg) == 1 and $arg > 0 and $arg < 7) {
        if ($hoshi ne $arg) {
          $match = 0;
          last;
        }
      }
      # if $arg is 火,水,風,光,闇 then match with zokusei only.
      if ($arg eq "火" or $arg eq "水" or $arg eq "風" or $arg eq "光" or $arg eq "闇") {
        if ($zokusei ne $arg) {
          $match = 0;
          last;
        }
      }
      # case insensitive match.
      if ($_ !~ /$arg/i) {
        $match = 0;
      }
    }
    if ($match == 1) {
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
  }
}
close IN;

if ($sortbyMP == 1) {
  foreach my $id (sort{ $otomes{$b}->{'mp'} <=> $otomes{$a}->{'mp'} or
    $otomes{$a}->{'zokusei'} cmp $otomes{$b}->{'zokusei'} or
    $otomes{$b}->{'hoshi'} <=> $otomes{$a}->{'hoshi'} or
    $otomes{$b}->{'bunrui'} cmp $otomes{$a}->{'bunrui'} or
    $otomes{$b}->{'shot'} cmp $otomes{$a}->{'shot'}
    } keys(%otomes)) {
    print_line($id);
  }
} else {
  # Arghhh.... yuck.
  foreach my $id (sort{ $otomes{$a}->{'zokusei'} cmp $otomes{$b}->{'zokusei'} or
    $otomes{$b}->{'hoshi'} <=> $otomes{$a}->{'hoshi'} or
    $otomes{$b}->{'bunrui'} cmp $otomes{$a}->{'bunrui'} or
    $otomes{$b}->{'shot'} cmp $otomes{$a}->{'shot'} or
    $otomes{$b}->{'mp'} <=> $otomes{$a}->{'mp'}
    } keys(%otomes)) {
    print_line($id);
  }
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
