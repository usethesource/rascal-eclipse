module demo::lang::Lisra::Console

import demo::lang::Lisra::Runtime;
import demo::lang::Lisra::Parse;
import demo::lang::Lisra::Eval;
import demo::lang::Lisra::Pretty;
import util::REPL;

@doc{Parse and evaluate an expression.}
public Result eval(str txt, Env env) = eval(parse(txt), env); /*1*/

@doc{Create an interactive console.}
public void console() {                                       /*2*/
   env = emptyEnv;
   
   Content command(str line) {
     <val, env> = eval(line, env);
     return text("<pretty(val)>");
   }
   
   Completion complete(str _, int cursor) {
     return <cursor, []>;
   }
   
   R = repl(title="Lisra Console",                            /*3*/
            welcome="Welcome to the Awesome Lisra Interpreter", 
            prompt="lisra\>",
            history=|home:///.lisra-history|,
            handler=command,
            completor=complete);
        
   startREPL(R);
}

