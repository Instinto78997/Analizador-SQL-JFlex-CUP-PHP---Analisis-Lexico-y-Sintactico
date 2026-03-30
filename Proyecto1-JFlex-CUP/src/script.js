// Variable para guardar el ultimo analisis realizado
let ultimoAnalisis = [];

// Funcion para analizar el codigo SQL
async function analizarSQL() {
    const sqlCode = document.getElementById('sqlInput').value;
    
    // Validar que haya ingresado algo
    if (!sqlCode.trim()) {
        mostrarStatus('Por favor ingrese codigo SQL para analizar', 'warning');
        return;
    }
    
    mostrarStatus('Analizando codigo SQL con JFlex y CUP...', 'loading');
    
    try {
        const formData = new FormData();
        formData.append('sql', sqlCode);
        
        const response = await fetch('analizar.php', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        ultimoAnalisis = data.tokens || [];
        
        if (data.valido) {
            mostrarStatus(data.mensaje, 'success');
        } else {
            mostrarStatus(data.mensaje, 'error');
        }
        
        actualizarTabla(ultimoAnalisis);
        
    } catch (error) {
        mostrarStatus('Error al conectar con el servidor: ' + error.message, 'error');
    }
}

// Funcion para subir archivo .sql
async function subirArchivo(file) {
    if (!file) return;
    
    const formData = new FormData();
    formData.append('file', file);
    
    mostrarStatus('Subiendo archivo ' + file.name + ' y analizando...', 'loading');
    
    try {
        const response = await fetch('analizar.php', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        ultimoAnalisis = data.tokens || [];
        
        if (data.valido) {
            mostrarStatus(data.mensaje, 'success');
            // Mostrar el contenido del archivo en el textarea
            const text = await file.text();
            document.getElementById('sqlInput').value = text;
        } else {
            mostrarStatus(data.mensaje, 'error');
        }
        
        actualizarTabla(ultimoAnalisis);
        
    } catch (error) {
        mostrarStatus('Error al procesar el archivo: ' + error.message, 'error');
    }
}

// Funcion para descargar la tabla en formato CSV
function descargarTabla() {
    if (!ultimoAnalisis || ultimoAnalisis.length === 0) {
        mostrarStatus('No hay datos para descargar', 'warning');
        return;
    }
    
    let csv = "No.,Linea,Columna,Lexema,Tipo,Descripcion\n";
    
    ultimoAnalisis.forEach(token => {
        csv += `${token.no},${token.linea},${token.columna},"${token.lexema}","${token.tipo}","${token.descripcion}"\n`;
    });
    
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `analisis_sql_${new Date().toISOString().slice(0,19)}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    mostrarStatus('Tabla descargada exitosamente', 'success');
}

// Funcion para limpiar el formulario
function limpiar() {
    document.getElementById('sqlInput').value = '';
    ultimoAnalisis = [];
    actualizarTabla([]);
    mostrarStatus('Todo ha sido limpiado', 'info');
}

// Funcion para actualizar la tabla de resultados
function actualizarTabla(tokens) {
    const tbody = document.getElementById('tableBody');
    
    if (!tokens || tokens.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty-message">No hay datos para mostrar</td></tr>';
        return;
    }
    
    tbody.innerHTML = tokens.map(token => `
        <tr class="${token.tipo === 'ERROR' ? 'error-row' : ''}">
            <td>${token.no}</td>
            <td>${token.linea}</td>
            <td>${token.columna}</td>
            <td><code>${escapeHtml(token.lexema)}</code></td>
            <td><span class="token-type">${escapeHtml(token.tipo)}</span></td>
            <td>${escapeHtml(token.descripcion)}</td>
        </tr>
    `).join('');
}

// Funcion para mostrar mensajes de estado
function mostrarStatus(mensaje, tipo) {
    const statusDiv = document.getElementById('status');
    let icono = '';
    
    switch(tipo) {
        case 'success': icono = '[ok]'; break;
        case 'error': icono = '[x]'; break;
        case 'warning': icono = '[!]'; break;
        case 'loading': icono = '[*]'; break;
        default: icono = '[i]';
    }
    
    statusDiv.innerHTML = `<span class="status-icon">${icono}</span><span>${mensaje}</span>`;
    statusDiv.className = `status ${tipo}`;
    
    if (tipo !== 'loading') {
        setTimeout(() => {
            if (statusDiv.className === `status ${tipo}`) {
                statusDiv.className = 'status';
            }
        }, 5000);
    }
}

// Funcion para escapar caracteres especiales en HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Atajo de teclado Ctrl+Enter para analizar
document.getElementById('sqlInput').addEventListener('keydown', (e) => {
    if (e.ctrlKey && e.key === 'Enter') {
        analizarSQL();
    }
});