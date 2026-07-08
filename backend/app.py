from flask import Flask, request, jsonify
from flask_cors import CORS
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

app = Flask(__name__)
CORS(app)

# Gmail configuration
GMAIL_ADDRESS = 'sv5323815@gmail.com'
GMAIL_PASSWORD = 'YOUR_GMAIL_APP_PASSWORD'  # Replace with your Gmail App Password
RECIPIENT_EMAIL = 'sv5323815@gmail.com'

def send_email(subject, body, to_email):
    """Send email using Gmail SMTP"""
    try:
        msg = MIMEMultipart()
        msg['From'] = GMAIL_ADDRESS
        msg['To'] = to_email
        msg['Subject'] = subject
        
        msg.attach(MIMEText(body, 'plain'))
        
        # Connect to Gmail SMTP server
        server = smtplib.SMTP('smtp.gmail.com', 587)
        server.starttls()
        server.login(GMAIL_ADDRESS, GMAIL_PASSWORD)
        server.send_message(msg)
        server.quit()
        
        return True
    except Exception as e:
        print(f"Error sending email: {e}")
        return False

@app.route('/submit', methods=['POST'])
def submit_form():
    try:
        data = request.json
        
        # Extract form data
        firstName = data.get('firstName', '')
        lastName = data.get('lastName', '')
        email = data.get('email', '')
        phone = data.get('phone', '')
        institution = data.get('institution', '')
        dateRequested = data.get('dateRequested', '')
        timeRequested = data.get('timeRequested', '')
        purpose = data.get('purpose', '')
        facility = data.get('facility', '')
        participants = data.get('participants', '')
        requests = data.get('requests', '')
        
        # Build email body
        email_body = f"""
DOST AMCen Booking Request

CUSTOMER INFORMATION
Name: {firstName} {lastName}
Email: {email}
Phone: {phone}
Institution: {institution or 'N/A'}

BOOKING DETAILS
Date: {dateRequested}
Time: {timeRequested}
Purpose: {purpose}

SERVICE INFORMATION
Equipment/Facility: {facility}
Number of Participants: {participants or 'N/A'}
Additional Requests:
{requests or 'None'}

---
This email was sent from the DOST AMCen Booking System
"""
        
        # Send email to admin
        subject = f'DOST AMCen Booking Request - {firstName} {lastName}'
        send_email(subject, email_body, RECIPIENT_EMAIL)
        
        # Send confirmation to customer
        confirmation_body = f"""Dear {firstName},

Thank you for submitting your booking request to DOST AMCen.

Your request has been received and will be reviewed shortly.
You will receive a confirmation email once your booking is approved.

Booking Details:
- Date: {dateRequested}
- Time: {timeRequested}
- Purpose: {purpose}

Best regards,
DOST AMCen Team
"""
        send_email('Booking Request Confirmation - DOST AMCen', confirmation_body, email)
        
        return jsonify({'success': True, 'message': 'Email sent successfully!'})
    
    except Exception as e:
        print(f"Error: {e}")
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/')
def serve_form():
    """Serve the HTML form"""
    return '''
    <!DOCTYPE html>
    <html>
    <head>
        <title>DOST AMCen Booking Form</title>
    </head>
    <body>
        <h1>Loading form...</h1>
        <p>Open your HTML file directly in the browser</p>
    </body>
    </html>
    '''

if __name__ == '__main__':
    print("Server running at http://localhost:5000")
    print("Make sure to update GMAIL_PASSWORD in this file!")
    app.run(debug=True, port=5000)
