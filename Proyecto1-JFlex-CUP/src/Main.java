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
        try {
            Lexer lexer = new Lexer(new StringReader(codigo));
            parser p = new parser(lexer);
            
            // Crear objeto para respuesta JSON manual
            StringBuilder json = new StringBuilder();
            
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
                
                json.append("{\n");
                json.append("  \"valido\": false,\n");
                json.append("  \"mensaje\": \"Error sintactico: ").append(escapeJSON(e.getMessage())).append("\",\n");
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
            }
            
            return json.toString();
            
        } catch (Exception e) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"valido\": false,\n");
            json.append("  \"mensaje\": \"Error al analizar: ").append(escapeJSON(e.getMessage())).append("\",\n");
            json.append("  \"tokens\": []\n");
            json.append("}");
            return json.toString();
        }
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