const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');
require('dotenv').config();

const app = express();

// Store active sessions (in production, use Redis or database)
const sessions = {};

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// SQLite Database Setup
const dbPath = path.join(__dirname, 'amcen_bookings.db');
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('Database error:', err);
  } else {
    console.log('Connected to SQLite database at:', dbPath);
  }
});

// Create bookings table if it doesn't exist
async function initializeDatabase() {
  return new Promise((resolve, reject) => {
    // Create bookings table
    db.run(`
      CREATE TABLE IF NOT EXISTS bookings (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        firstName TEXT NOT NULL,
        lastName TEXT NOT NULL,
        email TEXT NOT NULL,
        phone TEXT NOT NULL,
        institution TEXT,
        dateRequested TEXT NOT NULL,
        timeRequested TEXT NOT NULL,
        purpose TEXT NOT NULL,
        facility TEXT NOT NULL,
        participants INTEGER,
        requests TEXT,
        status TEXT DEFAULT 'pending',
        createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
        updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
        notes TEXT
      )
    `, (err) => {
      if (err) {
        console.error('Bookings table error:', err);
        reject(err);
        return;
      }

      // Create invoices table
      db.run(`
        CREATE TABLE IF NOT EXISTS invoices (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          bookingId INTEGER NOT NULL,
          email TEXT NOT NULL,
          subject TEXT NOT NULL,
          content TEXT NOT NULL,
          type TEXT DEFAULT 'confirmation',
          status TEXT DEFAULT 'pending',
          createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
          sentAt DATETIME,
          FOREIGN KEY (bookingId) REFERENCES bookings(id)
        )
      `, (err) => {
        if (err) {
          console.error('Invoices table error:', err);
          reject(err);
          return;
        }

        // Create admin users table
        db.run(`
          CREATE TABLE IF NOT EXISTS admin_users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            databaseId TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            name TEXT,
            email TEXT,
            role TEXT DEFAULT 'staff',
            active INTEGER DEFAULT 1,
            createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
            lastLogin DATETIME
          )
        `, (err) => {
          if (err) {
            console.error('Admin users table error:', err);
            reject(err);
            return;
          }

          // Check if default admin exists
          db.get('SELECT * FROM admin_users WHERE databaseId = ?', ['admin'], (err, row) => {
            if (err) {
              console.error('Check admin error:', err);
              reject(err);
              return;
            }

            if (!row) {
              // Create default admin user
              const hashedPassword = hashPassword('admin123');
              db.run(
                'INSERT INTO admin_users (databaseId, password, name, role) VALUES (?, ?, ?, ?)',
                ['admin', hashedPassword, 'System Administrator', 'admin'],
                (err) => {
                  if (err) {
                    console.error('Create admin error:', err);
                    reject(err);
                  } else {
                    console.log('✓ Default admin user created: ID: admin, Password: admin123');
                    console.log('✓ Database initialized successfully');
                    resolve();
                  }
                }
              );
            } else {
              console.log('✓ Database initialized successfully');
              resolve();
            }
          });
        });
      });
    });
  });
}

// Hash password function
function hashPassword(password) {
  return crypto.createHash('sha256').update(password).digest('hex');
}

// Generate invoice/mail for booking
function generateInvoice(booking, type = 'confirmation') {
  const statusMessages = {
    confirmation: `Your booking has been received. We will review it and send you a confirmation email soon.`,
    approved: `Your booking has been approved! You're all set for ${booking.dateRequested} at ${booking.timeRequested}.`,
    rejected: `Unfortunately, your booking request has been rejected. Please contact us for more information.`,
    completed: `Your booking has been completed. Thank you for using our facility!`
  };

  const subject = {
    confirmation: `Booking Confirmation - ${booking.facility}`,
    approved: `Booking Approved - ${booking.facility}`,
    rejected: `Booking Rejected - ${booking.facility}`,
    completed: `Booking Completed - ${booking.facility}`
  };

  const content = `
    <html>
      <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
        <h2>Booking ${type.charAt(0).toUpperCase() + type.slice(1)}</h2>
        <p>${statusMessages[type]}</p>
        
        <h3>Booking Details:</h3>
        <table style="border-collapse: collapse; width: 100%; margin: 20px 0;">
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Name:</td>
            <td style="padding: 8px;">${booking.firstName} ${booking.lastName}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Email:</td>
            <td style="padding: 8px;">${booking.email}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Phone:</td>
            <td style="padding: 8px;">${booking.phone}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Institution:</td>
            <td style="padding: 8px;">${booking.institution || 'N/A'}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Date Requested:</td>
            <td style="padding: 8px;">${booking.dateRequested}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Time Requested:</td>
            <td style="padding: 8px;">${booking.timeRequested}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Facility:</td>
            <td style="padding: 8px;">${booking.facility}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Purpose:</td>
            <td style="padding: 8px;">${booking.purpose}</td>
          </tr>
          <tr style="border-bottom: 1px solid #ddd;">
            <td style="padding: 8px; font-weight: bold;">Participants:</td>
            <td style="padding: 8px;">${booking.participants || 'N/A'}</td>
          </tr>
        </table>

        <p>If you have any questions, please contact us.</p>
        <p>Thank you,<br>Amcen Booking System</p>
      </body>
    </html>
  `;

  return { subject: subject[type], content };
}

// Middleware to check authentication
function authMiddleware(req, res, next) {
  const token = req.headers['authorization']?.split(' ')[1] || req.body.token || req.query.token;
  
  if (!token) {
    return res.status(401).json({ error: 'No token provided' });
  }

  if (!sessions[token]) {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }

  req.user = sessions[token];
  next();
}

// Routes

// Admin Login
app.post('/api/admin/login', (req, res) => {
  try {
    const { databaseId, password } = req.body;

    if (!databaseId || !password) {
      return res.status(400).json({ error: 'Database ID and password required' });
    }

    db.get('SELECT * FROM admin_users WHERE databaseId = ? AND active = 1', [databaseId], (err, user) => {
      if (err || !user) {
        return res.status(401).json({ error: 'Invalid database ID or password' });
      }

      const hashedPassword = hashPassword(password);

      if (user.password !== hashedPassword) {
        return res.status(401).json({ error: 'Invalid database ID or password' });
      }

      // Update last login
      db.run('UPDATE admin_users SET lastLogin = CURRENT_TIMESTAMP WHERE id = ?', [user.id]);

      // Generate token
      const token = crypto.randomBytes(32).toString('hex');
      sessions[token] = {
        id: user.id,
        databaseId: user.databaseId,
        name: user.name,
        role: user.role
      };

      res.json({
        success: true,
        message: 'Login successful',
        token: token,
        user: {
          id: user.id,
          databaseId: user.databaseId,
          name: user.name,
          role: user.role
        }
      });
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ error: 'Login failed' });
  }
});

// Admin Logout
app.post('/api/admin/logout', (req, res) => {
  try {
    const token = req.headers['authorization']?.split(' ')[1];
    if (token && sessions[token]) {
      delete sessions[token];
    }
    res.json({ success: true, message: 'Logged out successfully' });
  } catch (error) {
    res.status(500).json({ error: 'Logout failed' });
  }
});

// Submit booking (no auth required - for public form)
app.post('/api/bookings/submit', (req, res) => {
  try {
    const { firstName, lastName, email, phone, institution, dateRequested, timeRequested, purpose, facility, participants, requests } = req.body;
    
    // Validation
    if (!firstName || !lastName || !email || !phone || !dateRequested || !timeRequested || !purpose || !facility) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    db.run(
      'INSERT INTO bookings (firstName, lastName, email, phone, institution, dateRequested, timeRequested, purpose, facility, participants, requests, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
      [firstName, lastName, email, phone, institution, dateRequested, timeRequested, purpose, facility, participants || null, requests || null, 'pending'],
      function(err) {
        if (err) {
          console.error('Submission error:', err);
          return res.status(500).json({ error: 'Failed to submit booking' });
        }

        const bookingId = this.lastID;

        // Get the booking details
        db.get('SELECT * FROM bookings WHERE id = ?', [bookingId], (err, booking) => {
          if (err || !booking) {
            return res.status(201).json({
              success: true,
              message: 'Booking submitted successfully',
              bookingId: bookingId
            });
          }

          // Generate confirmation invoice
          const invoice = generateInvoice(booking, 'confirmation');

          // Create invoice record
          db.run(
            'INSERT INTO invoices (bookingId, email, subject, content, type) VALUES (?, ?, ?, ?, ?)',
            [bookingId, email, invoice.subject, invoice.content, 'confirmation'],
            (err) => {
              if (err) {
                console.error('Invoice creation error:', err);
              } else {
                console.log(`✓ Confirmation invoice created for booking ${bookingId}`);
              }

              res.status(201).json({
                success: true,
                message: 'Booking submitted successfully. Confirmation email sent.',
                bookingId: bookingId
              });
            }
          );
        });
      }
    );
  } catch (error) {
    console.error('Submission error:', error);
    res.status(500).json({ error: 'Failed to submit booking' });
  }
});

// Get all bookings (for Android client) - REQUIRES AUTH
app.get('/api/bookings', authMiddleware, (req, res) => {
  try {
    db.all('SELECT * FROM bookings ORDER BY createdAt DESC', (err, bookings) => {
      if (err) {
        console.error('Fetch error:', err);
        return res.status(500).json({ error: 'Failed to fetch bookings' });
      }
      
      res.json({
        success: true,
        data: bookings || []
      });
    });
  } catch (error) {
    console.error('Fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch bookings' });
  }
});

// Get booking by ID (for Android client) - REQUIRES AUTH
app.get('/api/bookings/:id', authMiddleware, (req, res) => {
  try {
    const { id } = req.params;
    db.get('SELECT * FROM bookings WHERE id = ?', [id], (err, booking) => {
      if (err) {
        return res.status(500).json({ error: 'Failed to fetch booking' });
      }
      
      if (!booking) {
        return res.status(404).json({ error: 'Booking not found' });
      }
      
      res.json({
        success: true,
        data: booking
      });
    });
  } catch (error) {
    console.error('Fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch booking' });
  }
});

// Update booking status (for Android client management) - REQUIRES AUTH
app.put('/api/bookings/:id/status', authMiddleware, (req, res) => {
  try {
    const { id } = req.params;
    const { status, notes } = req.body;

    if (!['pending', 'approved', 'rejected', 'completed'].includes(status)) {
      return res.status(400).json({ error: 'Invalid status' });
    }

    // Get booking first to get email
    db.get('SELECT * FROM bookings WHERE id = ?', [id], (err, booking) => {
      if (err || !booking) {
        return res.status(404).json({ error: 'Booking not found' });
      }

      // Update booking status
      db.run('UPDATE bookings SET status = ?, notes = ?, updatedAt = CURRENT_TIMESTAMP WHERE id = ?', [status, notes || null, id], (err) => {
        if (err) {
          return res.status(500).json({ error: 'Failed to update booking' });
        }

        // Generate invoice for status change
        const invoice = generateInvoice(booking, status);

        // Create invoice record
        db.run(
          'INSERT INTO invoices (bookingId, email, subject, content, type) VALUES (?, ?, ?, ?, ?)',
          [id, booking.email, invoice.subject, invoice.content, status],
          (err) => {
            if (err) {
              console.error('Invoice creation error:', err);
            } else {
              console.log(`✓ ${status} invoice created for booking ${id}`);
            }

            res.json({
              success: true,
              message: 'Booking status updated and notification sent'
            });
          }
        );
      });
    });
  } catch (error) {
    console.error('Update error:', error);
    res.status(500).json({ error: 'Failed to update booking' });
  }
});

// Filter bookings by status (for Android client) - REQUIRES AUTH
app.get('/api/bookings/filter/status/:status', authMiddleware, (req, res) => {
  try {
    const { status } = req.params;
    db.all('SELECT * FROM bookings WHERE status = ? ORDER BY createdAt DESC', [status], (err, bookings) => {
      if (err) {
        return res.status(500).json({ error: 'Failed to filter bookings' });
      }

      res.json({
        success: true,
        data: bookings || []
      });
    });
  } catch (error) {
    console.error('Filter error:', error);
    res.status(500).json({ error: 'Failed to filter bookings' });
  }
});

// Search bookings (for Android client) - REQUIRES AUTH
app.get('/api/bookings/search', authMiddleware, (req, res) => {
  try {
    const { query } = req.query;

    if (!query) {
      return res.status(400).json({ error: 'Search query required' });
    }

    const searchTerm = `%${query}%`;
    db.all(
      'SELECT * FROM bookings WHERE firstName LIKE ? OR lastName LIKE ? OR email LIKE ? OR institution LIKE ? ORDER BY createdAt DESC',
      [searchTerm, searchTerm, searchTerm, searchTerm],
      (err, bookings) => {
        if (err) {
          return res.status(500).json({ error: 'Failed to search bookings' });
        }

        res.json({
          success: true,
          data: bookings || []
        });
      }
    );
  } catch (error) {
    console.error('Search error:', error);
    res.status(500).json({ error: 'Failed to search bookings' });
  }
});

// Get all invoices - REQUIRES AUTH
app.get('/api/invoices', authMiddleware, (req, res) => {
  try {
    db.all('SELECT i.*, b.firstName, b.lastName, b.facility FROM invoices i JOIN bookings b ON i.bookingId = b.id ORDER BY i.createdAt DESC', (err, invoices) => {
      if (err) {
        console.error('Fetch invoices error:', err);
        return res.status(500).json({ error: 'Failed to fetch invoices' });
      }
      
      res.json({
        success: true,
        data: invoices || []
      });
    });
  } catch (error) {
    console.error('Fetch invoices error:', error);
    res.status(500).json({ error: 'Failed to fetch invoices' });
  }
});

// Get invoices for specific booking - REQUIRES AUTH
app.get('/api/invoices/booking/:bookingId', authMiddleware, (req, res) => {
  try {
    const { bookingId } = req.params;
    db.all('SELECT * FROM invoices WHERE bookingId = ? ORDER BY createdAt DESC', [bookingId], (err, invoices) => {
      if (err) {
        return res.status(500).json({ error: 'Failed to fetch invoices' });
      }
      
      res.json({
        success: true,
        data: invoices || []
      });
    });
  } catch (error) {
    console.error('Fetch invoices error:', error);
    res.status(500).json({ error: 'Failed to fetch invoices' });
  }
});

// Get invoices by type - REQUIRES AUTH
app.get('/api/invoices/type/:type', authMiddleware, (req, res) => {
  try {
    const { type } = req.params;
    db.all('SELECT i.*, b.firstName, b.lastName, b.facility FROM invoices i JOIN bookings b ON i.bookingId = b.id WHERE i.type = ? ORDER BY i.createdAt DESC', [type], (err, invoices) => {
      if (err) {
        return res.status(500).json({ error: 'Failed to fetch invoices' });
      }
      
      res.json({
        success: true,
        data: invoices || []
      });
    });
  } catch (error) {
    console.error('Fetch invoices error:', error);
    res.status(500).json({ error: 'Failed to fetch invoices' });
  }
});

// Delete booking - REQUIRES AUTH
app.delete('/api/bookings/:id', authMiddleware, (req, res) => {
  try {
    const { id } = req.params;
    db.run('DELETE FROM bookings WHERE id = ?', [id], (err) => {
      if (err) {
        return res.status(500).json({ error: 'Failed to delete booking' });
      }

      res.json({
        success: true,
        message: 'Booking deleted'
      });
    });
  } catch (error) {
    console.error('Delete error:', error);
    res.status(500).json({ error: 'Failed to delete booking' });
  }
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'API is running', database: 'SQLite' });
});

// Initialize database and start server
const PORT = process.env.PORT || 5000;

initializeDatabase().then(() => {
  app.listen(PORT, () => {
    console.log(`\n========================================`);
    console.log(`✓ Server running on http://localhost:${PORT}`);
    console.log(`✓ Database: ${dbPath}`);
    console.log(`========================================\n`);
  });
}).catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});
