<?php
// send_booking.php - Handles booking request emails

// Set headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Get JSON data from request
$json = file_get_contents('php://input');
$data = json_decode($json, true);

// Email configuration
$recipientEmail = 'sv5323815@gmail.com'; // Change to your email
$senderEmail = $data['email'] ?? 'noreply@dostamcen.com';
$senderName = ($data['firstName'] ?? '') . ' ' . ($data['lastName'] ?? '');

// Validate required fields
$requiredFields = ['firstName', 'lastName', 'email', 'phone', 'dateRequested', 'timeRequested', 'purpose', 'facility'];
foreach($requiredFields as $field){
    if(empty($data[$field])){
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => "Missing required field: $field"]);
        exit;
    }
}

// Validate email format
if(!filter_var($data['email'], FILTER_VALIDATE_EMAIL)){
    http_response_code(400);
    echo json_encode(['success' => false, 'error' => 'Invalid email address']);
    exit;
}

// Build email subject
$subject = 'DOST AMCen Booking Request - ' . $data['firstName'] . ' ' . $data['lastName'];

// Build email body
$emailBody = "BOOKING REQUEST DETAILS\n";
$emailBody .= "=======================\n\n";

$emailBody .= "CUSTOMER INFORMATION\n";
$emailBody .= "Name: " . htmlspecialchars($data['firstName']) . " " . htmlspecialchars($data['lastName']) . "\n";
$emailBody .= "Email: " . htmlspecialchars($data['email']) . "\n";
$emailBody .= "Phone: " . htmlspecialchars($data['phone']) . "\n";
$emailBody .= "Institution: " . htmlspecialchars($data['institution'] ?? 'N/A') . "\n\n";

$emailBody .= "BOOKING DETAILS\n";
$emailBody .= "Date: " . htmlspecialchars($data['dateRequested']) . "\n";
$emailBody .= "Time: " . htmlspecialchars($data['timeRequested']) . "\n";
$emailBody .= "Purpose: " . htmlspecialchars($data['purpose']) . "\n\n";

$emailBody .= "SERVICE INFORMATION\n";
$emailBody .= "Equipment/Facility: " . htmlspecialchars($data['facility']) . "\n";
$emailBody .= "Number of Participants: " . htmlspecialchars($data['participants'] ?? 'N/A') . "\n";
$emailBody .= "Additional Requests:\n" . htmlspecialchars($data['requests'] ?? 'None') . "\n\n";

$emailBody .= "---\n";
$emailBody .= "This email was sent from the DOST AMCen Booking System\n";
$emailBody .= "Please do not reply to this automated email.\n";

// Email headers
$headers = "From: " . $senderName . " <" . $senderEmail . ">\r\n";
$headers .= "Reply-To: " . $senderEmail . "\r\n";
$headers .= "Content-Type: text/plain; charset=UTF-8\r\n";
$headers .= "X-Mailer: DOST AMCen Booking System\r\n";

// Send email
$mailSent = mail($recipientEmail, $subject, $emailBody, $headers);

if($mailSent){
    // Also send confirmation email to customer
    $confirmationSubject = "Booking Request Confirmation - DOST AMCen";
    $confirmationBody = "Dear " . htmlspecialchars($data['firstName']) . ",\n\n";
    $confirmationBody .= "Thank you for submitting your booking request to DOST AMCen.\n\n";
    $confirmationBody .= "Your request has been received and will be reviewed shortly.\n";
    $confirmationBody .= "You will receive a confirmation email once your booking is approved.\n\n";
    $confirmationBody .= "Booking Details:\n";
    $confirmationBody .= "- Date: " . htmlspecialchars($data['dateRequested']) . "\n";
    $confirmationBody .= "- Time: " . htmlspecialchars($data['timeRequested']) . "\n";
    $confirmationBody .= "- Purpose: " . htmlspecialchars($data['purpose']) . "\n\n";
    $confirmationBody .= "Best regards,\n";
    $confirmationBody .= "DOST AMCen Team\n";

    $confirmationHeaders = "From: DOST AMCen <noreply@dostamcen.com>\r\n";
    $confirmationHeaders .= "Content-Type: text/plain; charset=UTF-8\r\n";

    mail($data['email'], $confirmationSubject, $confirmationBody, $confirmationHeaders);

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Booking request submitted successfully!'
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Failed to send booking request. Please try again later.'
    ]);
}
?>
