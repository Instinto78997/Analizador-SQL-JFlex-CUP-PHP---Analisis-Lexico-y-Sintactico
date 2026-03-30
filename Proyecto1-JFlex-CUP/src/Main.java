import java.io.*;
import java_cup.runtime.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java Main <archivo_sql>");
            System.exit(1);
        }
        
        try {
            String filename = args[0];
            String sqlCode = leerArchivo(filename);
            String resultado = analizarSQL(sqlCode);
            System.out.println(resultado);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static String leerArchivo(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line).append("\n");
        }
        br.close();
        return content.toString();
    }
    
    private static String analizarSQL(String codigo) {
        StringBuilder json = new StringBuilder();
        try {
            Lexer lexer = new Lexer(new StringReader(codigo));
            parser p = new parser(lexer);
            
            try {
                p.parse();
                List<Lexer.ResultadoToken> tokens = lexer.getResultados();
                
                json.append("{\n");
                json.append("  \"valido\": true,\n");
                json.append("  \"mensaje\": \"Analisis completado exitosamente\",\n");
                json.append("  \"tokens\": [\n");
                
                for (int i = 0; i < tokens.size(); i++) {
                    Lexer.ResultadoToken t = tokens.get(i);
                    json.append("    {");
                    json.append("\"no\":").append(t.no).append(",");
                    json.append("\"linea\":").append(t.linea).append(",");
                    json.append("\"columna\":").append(t.columna).append(",");
                    json.append("\"lexema\":\"").append(escapeJSON(t.lexema)).append("\",");
                    json.append("\"tipo\":\"").append(escapeJSON(t.tipo)).append("\",");
                    json.append("\"descripcion\":\"").append(escapeJSON(t.descripcion)).append("\"");
                    json.append("}");
                    if (i < tokens.size() - 1) json.append(",");
                    json.append("\n");
                }
                
                json.append("  ]\n");
                json.append("}");
                
            } catch (Exception e) {
                List<Lexer.ResultadoToken> tokens = lexer.getResultados();
                
                // Obtener el ultimo token procesado
                Lexer.ResultadoToken lastToken = null;
                int errorLinea = 1;
                int errorColumna = 1;
                String errorToken = "";
                
                if (!tokens.isEmpty()) {
                    lastToken = tokens.get(tokens.size() - 1);
                    errorLinea = lastToken.linea;
                    errorColumna = lastToken.columna;
                    errorToken = lastToken.lexema;
                }
                
                // Extraer mensaje de error de CUP
                String errorMensaje = e.getMessage();
                if (errorMensaje == null || errorMensaje.isEmpty()) {
                    errorMensaje = "Error de sintaxis";
                }
                
                json.append("{\n");
                json.append("  \"valido\": false,\n");
                json.append("  \"mensaje\": \"Error sintactico en Linea ").append(errorLinea);
                json.append(" Columna ").append(errorColumna).append(": '").append(escapeJSON(errorToken));
                json.append("' no esperado\",\n");
                json.append("  \"tokens\": [\n");
                
                // Agregar todos los tokens validos antes del error
                for (int i = 0; i < tokens.size(); i++) {
                    Lexer.ResultadoToken t = tokens.get(i);
                    json.append("    {");
                    json.append("\"no\":").append(t.no).append(",");
                    json.append("\"linea\":").append(t.linea).append(",");
                    json.append("\"columna\":").append(t.columna).append(",");
                    json.append("\"lexema\":\"").append(escapeJSON(t.lexema)).append("\",");
                    json.append("\"tipo\":\"").append(escapeJSON(t.tipo)).append("\",");
                    json.append("\"descripcion\":\"").append(escapeJSON(t.descripcion)).append("\"");
                    json.append("}");
                    if (i < tokens.size() - 1) json.append(",");
                    json.append("\n");
                }
                
                // Agregar el token que causo el error como ERROR
                if (!tokens.isEmpty()) {
                    if (tokens.size() > 0) json.append(",");
                    json.append("\n");
                }
                
                json.append("    {");
                json.append("\"no\":").append(tokens.size() + 1).append(",");
                json.append("\"linea\":").append(errorLinea).append(",");
                json.append("\"columna\":").append(errorColumna).append(",");
                json.append("\"lexema\":\"").append(escapeJSON(errorToken)).append("\",");
                json.append("\"tipo\":\"ERROR\",");
                json.append("\"descripcion\":\"ERROR SINTACTICO: '").append(escapeJSON(errorToken)).append("' no esperado aqui\"");
                json.append("}\n");
                
                json.append("  ]\n");
                json.append("}");
            }
            
        } catch (Exception e) {
            json.append("{\n");
            json.append("  \"valido\": false,\n");
            json.append("  \"mensaje\": \"Error al analizar: ").append(escapeJSON(e.getMessage())).append("\",\n");
            json.append("  \"tokens\": []\n");
            json.append("}");
        }
        
        return json.toString();
    }
    
    private static String escapeJSON(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}