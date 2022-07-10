grammar Filter;

@header {
package com.github.skibish.filterexample.antlr4;
}

filter: expr+ EOF ;

expr
 : OPAR expr CPAR
 | NOT PROPERTY COMPARISON VALUE
 | PROPERTY COMPARISON VALUE
 | expr AND expr
 | expr OR expr
 ;

OPAR: '(' ;
CPAR: ')' ;

OR  : 'or' ;
AND : 'and' ;

NOT : 'not' ;

COMPARISON: (GT | GE | LT | LE | EQ | NE);

GT : 'gt' ;
GE : 'ge' ;
LT : 'lt' ;
LE : 'le' ;
EQ : 'eq' ;
NE : 'ne' ;

VALUE
 : TRUE
 | FALSE
 | INT
 | FLOAT
 | STRING
 ;

TRUE
 : 'true'
 ;

FALSE
 : 'false'
 ;

PROPERTY
 : ALLOWED_CHARACTERS+ ('.' ALLOWED_CHARACTERS+)*
 ;

fragment ALLOWED_CHARACTERS : [a-zA-Z0-9_-];

STRING
 : '\'' (~['\r\n] | '\'\'')* '\''
 ;

INT
 : DIGIT+
 ;

FLOAT
 : DIGIT+ '.' DIGIT*
 | '.' DIGIT+
 ;

fragment DIGIT : [0-9] ;

SPACE
 : [ \t\r\n] -> skip
 ;