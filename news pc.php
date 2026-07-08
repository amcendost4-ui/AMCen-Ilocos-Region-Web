<?php
require_once __DIR__ . '/config.php';

function sendJson($data, $status = 200) {
    http_response_code($status);
    echo json_encode($data);
    exit;
}

function getPayload() {
    $body = file_get_contents('php://input');
    $json = json_decode($body, true);
    return is_array($json) ? $json : [];
}

function saveImagePath($imagePath) {
    if (!is_string($imagePath) || trim($imagePath) === '') {
        return null;
    }

    $imagePath = trim($imagePath);
    if (stripos($imagePath, 'data:image/') === 0) {
        $matches = [];
        if (!preg_match('/^data:image\/(png|jpeg|jpg|gif);base64,(.*)$/i', $imagePath, $matches)) {
            return null;
        }

        $extension = strtolower($matches[1]) === 'jpeg' ? 'jpg' : strtolower($matches[1]);
        $data = base64_decode($matches[2]);
        if ($data === false) {
            return null;
        }

        $uploadDir = realpath(__DIR__ . '/../uploads');
        if ($uploadDir === false) {
            $uploadDir = __DIR__ . '/../uploads';
        }

        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        $fileName = 'news_' . time() . '_' . bin2hex(random_bytes(6)) . '.' . $extension;
        $filePath = $uploadDir . '/' . $fileName;

        if (file_put_contents($filePath, $data) === false) {
            return null;
        }

        return 'uploads/' . $fileName;
    }

    if (stripos($imagePath, 'http://') === 0 || stripos($imagePath, 'https://') === 0) {
        return $imagePath;
    }

    return $imagePath;
}

$method = $_SERVER['REQUEST_METHOD'];
$id = isset($_GET['id']) ? intval($_GET['id']) : null;

if ($method === 'OPTIONS') {
    http_response_code(204);
    exit;
}

if ($method === 'GET') {
    if ($id) {
        $stmt = $pdo->prepare('SELECT * FROM news WHERE id = ?');
        $stmt->execute([$id]);
        $row = $stmt->fetch();
        if (!$row) {
            sendJson(['success' => false, 'message' => 'News not found'], 404);
        }
        sendJson($row);
    }

    $stmt = $pdo->query('SELECT * FROM news ORDER BY created_at DESC');
    $rows = $stmt->fetchAll();
    sendJson($rows);
}

if ($method === 'POST') {
    $payload = getPayload();
    $password = trim($payload['password'] ?? '');
    if ($password !== ADMIN_PASSWORD) {
        sendJson(['success' => false, 'message' => 'Unauthorized'], 401);
    }

    $title = trim($payload['title'] ?? '');
    $date = trim($payload['date'] ?? '');
    $category = trim($payload['category'] ?? '');
    $description = trim($payload['description'] ?? '');
    $imagePath = saveImagePath($payload['image_path'] ?? '');

    if ($title === '' || $date === '' || $category === '' || $description === '') {
        sendJson(['success' => false, 'message' => 'Title, date, category, and description are required'], 400);
    }

    if ($imagePath === null) {
        $imagePath = 'https://via.placeholder.com/300x180?text=No+Image';
    }

    $stmt = $pdo->prepare('INSERT INTO news (title, date, category, description, image_path, created_at) VALUES (?, ?, ?, ?, ?, ?)');
    $stmt->execute([$title, $date, $category, $description, $imagePath, time() * 1000]);
    $newsId = $pdo->lastInsertId();
    $stmt = $pdo->prepare('SELECT * FROM news WHERE id = ?');
    $stmt->execute([$newsId]);
    $row = $stmt->fetch();
    sendJson(['success' => true, 'message' => 'News created', 'data' => $row], 201);
}

if ($method === 'PUT') {
    if (!$id) {
        sendJson(['success' => false, 'message' => 'News ID is required'], 400);
    }

    $payload = getPayload();
    $password = trim($payload['password'] ?? '');
    if ($password !== ADMIN_PASSWORD) {
        sendJson(['success' => false, 'message' => 'Unauthorized'], 401);
    }

    $stmt = $pdo->prepare('SELECT * FROM news WHERE id = ?');
    $stmt->execute([$id]);
    $row = $stmt->fetch();
    if (!$row) {
        sendJson(['success' => false, 'message' => 'News not found'], 404);
    }

    $title = trim($payload['title'] ?? $row['title']);
    $date = trim($payload['date'] ?? $row['date']);
    $category = trim($payload['category'] ?? $row['category']);
    $description = trim($payload['description'] ?? $row['description']);
    $imagePath = $row['image_path'];
    if (isset($payload['image_path']) && trim($payload['image_path']) !== '') {
        $saved = saveImagePath($payload['image_path']);
        if ($saved !== null) {
            $imagePath = $saved;
        }
    }

    $stmt = $pdo->prepare('UPDATE news SET title = ?, date = ?, category = ?, description = ?, image_path = ? WHERE id = ?');
    $stmt->execute([$title, $date, $category, $description, $imagePath, $id]);
    $stmt = $pdo->prepare('SELECT * FROM news WHERE id = ?');
    $stmt->execute([$id]);
    $updated = $stmt->fetch();
    sendJson(['success' => true, 'message' => 'News updated', 'data' => $updated]);
}

if ($method === 'DELETE') {
    if (!$id) {
        sendJson(['success' => false, 'message' => 'News ID is required'], 400);
    }

    $payload = getPayload();
    $password = trim($payload['password'] ?? '');
    if ($password !== ADMIN_PASSWORD) {
        sendJson(['success' => false, 'message' => 'Unauthorized'], 401);
    }

    $stmt = $pdo->prepare('SELECT * FROM news WHERE id = ?');
    $stmt->execute([$id]);
    $row = $stmt->fetch();
    if (!$row) {
        sendJson(['success' => false, 'message' => 'News not found'], 404);
    }

    $stmt = $pdo->prepare('DELETE FROM news WHERE id = ?');
    $stmt->execute([$id]);
    sendJson(['success' => true, 'message' => 'News deleted', 'data' => $row]);
}

sendJson(['success' => false, 'message' => 'Method not allowed'], 405);
