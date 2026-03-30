<?php
// analizar.php - Version mejorada con manejo de errores

error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// Configuracion
$tempDir = __DIR__ . '/temp/';
if (!file_exists($tempDir)) {
    mkdir($tempDir, 0777, true);
}

// Obtener SQL
$sql = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_FILES['file']) && $_FILES['file']['error'] === UPLOAD_ERR_OK) {
        $sql = file_get_contents($_FILES['file']['tmp_name']);
    } elseif (isset($_POST['sql'])) {
        $sql = $_POST['sql'];
    }
} elseif (isset($_GET['sql'])) {
    $sql = $_GET['sql'];
}

if (empty($sql)) {
    echo json_encode(['valido' => false, 'mensaje' => 'No se proporciono codigo SQL para analizar', 'tokens' => []]);
    exit;
}

// Guardar SQL en archivo temporal
$sqlFile = $tempDir . 'input_' . uniqid() . '.sql';
file_put_contents($sqlFile, $sql);

// Cambiar al directorio src
$originalDir = getcwd();
chdir(__DIR__);

// Establecer el classpath
$classpath = '.;..\\tools\\jflex-full-1.9.1.jar;..\\tools\\java-cup-11b.jar';

// Ejecutar Java
$cmd = 'java -cp "' . $classpath . '" Main ' . escapeshellarg($sqlFile) . ' 2>&1';
exec($cmd, $output, $returnCode);

// Volver al directorio original
chdir($originalDir);

// Limpiar archivo temporal
unlink($sqlFile);

// Procesar salida
$outputStr = implode("\n", $output);
$result = json_decode($outputStr, true);

if ($result !== null) {
    echo json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
} else {
    // Buscar errores en la salida
    $errorMsg = $outputStr;
    
    // Extraer informacion del error si existe
    if (preg_match('/Error sintactico en Linea (\d+) Columna (\d+)/', $outputStr, $matches)) {
        $linea = $matches[1];
        $columna = $matches[2];
        $errorMsg = "Error sintactico en Linea $linea Columna $columna";
    }
    
    echo json_encode([
        'valido' => false, 
        'mensaje' => $errorMsg,
        'tokens' => []
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
}
?>