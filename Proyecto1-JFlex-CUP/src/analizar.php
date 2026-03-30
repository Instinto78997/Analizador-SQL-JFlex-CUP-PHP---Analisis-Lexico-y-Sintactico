<?php
// analizar.php - Recibe SQL, llama a Java y devuelve JSON

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

// Usar el mismo comando que funciono en la terminal
$currentDir = __DIR__;
$classpath = '.' . PATH_SEPARATOR . '..\\tools\\jflex-full-1.9.1.jar' . PATH_SEPARATOR . '..\\tools\\java-cup-11b.jar';

// Cambiar al directorio src para ejecutar el comando
$cmd = 'cd /d "' . $currentDir . '" && java -cp "' . $classpath . '" Main ' . escapeshellarg($sqlFile) . ' 2>&1';

// Ejecutar el comando
exec($cmd, $output, $returnCode);

// Limpiar archivo temporal
unlink($sqlFile);

// Procesar salida
$outputStr = implode("\n", $output);
$result = json_decode($outputStr, true);

if ($result !== null) {
    echo json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode([
        'valido' => false, 
        'mensaje' => 'Error en el analizador Java',
        'debug' => $outputStr,
        'cmd' => $cmd,
        'tokens' => []
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
}
?>