@doc{
Synopsis: a rudimentary definition for parsing concept files, for
use in the IDE
}
module lang::tutor::Syntax

layout Layout 
  = [\t\n\r\ ] !<< [\t\n\r\ ]* !>> [\t\n\r\ ]
  ;

start syntax ConceptFile
  = "Name" ":" Id name Text
  ;
syntax Text = Word*;
 
lexical Word = ![\t\n\r\ ] !<< ![\t\n\r\ ]+ !>> ![\t\n\r\ ]; 

lexical Id = [A-Za-z0-9] !<< [A-Za-z][A-Za-z0-9]* !>> [A-Za-z0-9];