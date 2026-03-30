%%
%public
%class Lexer
%cup
%line
%column
%unicode
%ignorecase

%{
    /* Clase para almacenar resultados del analisis */
    public static class ResultadoToken {
        public int no, linea, columna;
        public String lexema, tipo, descripcion;
        
        public ResultadoToken(int n, int l, int c, String lex, String tip, String desc) {
            this.no = n;
            this.linea = l;
            this.columna = c;
            this.lexema = lex;
            this.tipo = tip;
            this.descripcion = desc;
        }
    }
    
    private int tokenCount = 1;
    private java.util.ArrayList<ResultadoToken> resultados = new java.util.ArrayList<>();
    
    private java_cup.runtime.Symbol symbol(int type, String lexema, String descripcion) {
        ResultadoToken r = new ResultadoToken(tokenCount++, yyline+1, yycolumn+1, lexema, sym.terminalNames[type], descripcion);
        resultados.add(r);
        return new java_cup.runtime.Symbol(type, yyline+1, yycolumn+1, r);
    }
    
    private void error(String mensaje) {
        ResultadoToken r = new ResultadoToken(tokenCount++, yyline+1, yycolumn+1, yytext(), "ERROR", mensaje);
        resultados.add(r);
    }
    
    public java.util.ArrayList<ResultadoToken> getResultados() {
        return resultados;
    }
    
    public void limpiarResultados() {
        resultados.clear();
        tokenCount = 1;
    }
%}

/* Expresiones regulares */
Letra = [a-zA-Z]
Digito = [0-9]
Espacio = [ \t\r\f]
SaltoLinea = [\n]
Identificador = {Letra}({Letra}|{Digito}|"_")*
Entero = {Digito}+
Decimal = {Digito}+"."{Digito}+
Cadena = "'"[^'\n]*"'"

%%

"create"            { return symbol(sym.CREATE, yytext(), "CREATE"); }
"table"             { return symbol(sym.TABLE, yytext(), "TABLE"); }
"select"            { return symbol(sym.SELECT, yytext(), "SELECT"); }
"from"              { return symbol(sym.FROM, yytext(), "FROM"); }
"where"             { return symbol(sym.WHERE, yytext(), "WHERE"); }
"update"            { return symbol(sym.UPDATE, yytext(), "UPDATE"); }
"set"               { return symbol(sym.SET, yytext(), "SET"); }
"insert"            { return symbol(sym.INSERT, yytext(), "INSERT"); }
"into"              { return symbol(sym.INTO, yytext(), "INTO"); }
"values"            { return symbol(sym.VALUES, yytext(), "VALUES"); }
"int"               { return symbol(sym.INT, yytext(), "INT"); }
"varchar"           { return symbol(sym.VARCHAR, yytext(), "VARCHAR"); }
"decimal"           { return symbol(sym.DECIMAL, yytext(), "DECIMAL"); }
"datetime"          { return symbol(sym.DATETIME, yytext(), "DATETIME"); }

"("                 { return symbol(sym.LPAREN, yytext(), "("); }
")"                 { return symbol(sym.RPAREN, yytext(), ")"); }
","                 { return symbol(sym.COMMA, yytext(), ","); }
";"                 { return symbol(sym.SEMICOLON, yytext(), ";"); }
"*"                 { return symbol(sym.MULT, yytext(), "*"); }
"="                 { return symbol(sym.EQUALS, yytext(), "="); }
"."                 { return symbol(sym.DOT, yytext(), "."); }

{Identificador}     { return symbol(sym.IDENTIFICADOR, yytext(), "IDENTIFICADOR"); }
{Entero}            { return symbol(sym.NUMERO, yytext(), "ENTERO"); }
{Decimal}           { return symbol(sym.DECIMAL_VAL, yytext(), "DECIMAL"); }
{Cadena}            { return symbol(sym.CADENA, yytext(), "CADENA"); }

{Espacio}+          { /* Ignorar espacios */ }
{SaltoLinea}        { /* Ignorar saltos de linea */ }

[^]                 { error("Caracter no reconocido: " + yytext()); }