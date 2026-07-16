const express = require('express');
const nodemailer = require('nodemailer');
const cors = require('cors');
const bodyParser = require('body-parser');
const fs = require('fs');
const path = require('path');
const twilio = require('twilio');

const app = express();

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }));

// Admin password
const ADMIN_PASSWORD = 'admin123';

// News data file
const newsFile = path.join(__dirname, 'news.json');

// Initialize news file if it doesn't exist
if (!fs.existsSync(newsFile)) {
    fs.writeFileSync(newsFile, JSON.stringify([], null, 2));
}

// Helper to read news
const readNews = () => {
    try {
        const data = fs.readFileSync(newsFile, 'utf8');
        return JSON.parse(data);
    } catch {
        return [];
    }
};

// Helper to write news
const writeNews = (data) => {
    fs.writeFileSync(newsFile, JSON.stringify(data, null, 2));
};

// Gmail configuration - UPDATE WITH YOUR GMAIL AND APP PASSWORD
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'sv5323815@gmail.com',  // Your Gmail
        pass: 'YOUR_GMAIL_APP_PASSWORD'  // Gmail App Password (not your regular password)
    }
});

const TWILIO_ACCOUNT_SID = process.env.TWILIO_ACCOUNT_SID || '';
const TWILIO_AUTH_TOKEN = process.env.TWILIO_AUTH_TOKEN || '';
const TWILIO_PHONE_NUMBER = process.env.TWILIO_PHONE_NUMBER || '';

const hasTwilioConfig = Boolean(TWILIO_ACCOUNT_SID && TWILIO_AUTH_TOKEN && TWILIO_PHONE_NUMBER);
const twilioClient = hasTwilioConfig ? twilio(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN) : null;

// ============ NEWS API ENDPOINTS ============

// Check admin password
app.post('/api/admin/check-password', (req, res) => {
    const { password } = req.body;
    if (password === ADMIN_PASSWORD) {
        res.json({ success: true, message: 'Password correct' });
    } else {
        res.status(401).json({ success: false, message: 'Invalid password' });
    }
});

// Get all news
app.get('/api/news', (req, res) => {
    const news = readNews();
    res.json(news);
});

// Create news
app.post('/api/news', (req, res) => {
    const { title, date, category, description, password, image_path } = req.body;
    
    if (password !== ADMIN_PASSWORD) {
        return res.status(401).json({ success: false, message: 'Invalid password' });
    }
    
    const news = readNews();
    const newItem = {
        id: Date.now(),
        title,
        date,
        category,
        description,
        image_path
    };
    
    news.unshift(newItem);
    writeNews(news);
    res.json({ success: true, message: 'News posted', data: newItem });
});

// Update news
app.put('/api/news/:id', (req, res) => {
    const { id } = req.params;
    const { title, date, category, description, password, image_path } = req.body;
    
    if (password !== ADMIN_PASSWORD) {
        return res.status(401).json({ success: false, message: 'Invalid password' });
    }
    
    let news = readNews();
    const index = news.findIndex(item => item.id == id);
    
    if (index === -1) {
        return res.status(404).json({ success: false, message: 'News not found' });
    }
    
    news[index] = {
        ...news[index],
        title,
        date,
        category,
        description,
        image_path: image_path || news[index].image_path
    };
    
    writeNews(news);
    res.json({ success: true, message: 'News updated', data: news[index] });
});

// Delete news
app.delete('/api/news/:id', (req, res) => {
    const { id } = req.params;
    const { password } = req.body;
    
    if (password !== ADMIN_PASSWORD) {
        return res.status(401).json({ success: false, message: 'Invalid password' });
    }
    
    let news = readNews();
    const filtered = news.filter(item => item.id != id);
    
    if (filtered.length === news.length) {
        return res.status(404).json({ success: false, message: 'News not found' });
    }
    
    writeNews(filtered);
    res.json({ success: true, message: 'News deleted' });
});

// ============ BOOKING API ENDPOINTS ============

app.post('/api/sms/send', async (req, res) => {
    const { to, message } = req.body;

    if (!to || !message) {
        return res.status(400).json({ success: false, message: 'Recipient number and message are required' });
    }

    if (!hasTwilioConfig || !twilioClient) {
        return res.status(503).json({
            success: false,
            message: 'Twilio is not configured on the server',
        });
    }

    try {
        const result = await twilioClient.messages.create({
            from: TWILIO_PHONE_NUMBER,
            to,
            body: message,
        });

        res.json({
            success: true,
            message: 'SMS sent successfully',
            data: {
                sid: result.sid,
                status: result.status,
                to: result.to,
            },
        });
    } catch (error) {
        console.error('Error sending SMS:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Handle form submission
app.post('/submit', async (req, res) => {
    const { firstName, lastName, email, phone, institution, dateRequested, timeRequested, purpose, facility, participants, requests } = req.body;

    // Email content
    const emailContent = `
DOST AMCen Booking Request

CUSTOMER INFORMATION
Name: ${firstName} ${lastName}
Email: ${email}
Phone: ${phone}
Institution: ${institution || 'N/A'}

BOOKING DETAILS
Date: ${dateRequested}
Time: ${timeRequested}
Purpose: ${purpose}

SERVICE INFORMATION
Equipment/Facility: ${facility}
Participants: ${participants || 'N/A'}
Additional Requests: ${requests || 'None'}
    `;

    try {
        // Send email to your Gmail
        await transporter.sendMail({
            from: 'sv5323815@gmail.com',
            to: 'sv5323815@gmail.com',
            subject: `DOST AMCen Booking Request - ${firstName} ${lastName}`,
            text: emailContent
        });

        // Send confirmation to customer
        await transporter.sendMail({
            from: 'sv5323815@gmail.com',
            to: email,
            subject: 'Booking Request Confirmation - DOST AMCen',
            text: `Dear ${firstName},\n\nThank you for submitting your booking request to DOST AMCen.\n\nYour request has been received and will be reviewed shortly.\nYou will receive a confirmation email once your booking is approved.\n\nBest regards,\nDOST AMCen Team`
        });

        res.json({ success: true, message: 'Email sent successfully!' });
    } catch (error) {
        console.error('Error sending email:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Serve static files
app.use(express.static('.'));

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
