# This service is responsible for sending emails to users.
# It listens on port 41567 and accepts POST requests to /send_email.
# The request must contain the following fields:
# - recipient: The email address of the recipient
# - subject: The subject of the email
# - content: The content of the email
# The email is sent using the SMTP server running on localhost:25.
# If the email is sent successfully, the service returns a 200 status code.
# If there is an error, the service returns a 500 status code with an error message.

from flask import Flask, request, jsonify
from datetime import datetime
import smtplib
from email.mime.text import MIMEText

app = Flask(__name__)

SERVICE_PORT = 41567
SERVICE_URL = '/send_email'

SENDER_ADDRESS = 'gwent@apgrp10.ydns.eu'
SENDER_NAME = 'Gwent-Project'
SMTP_SERVER = 'localhost'
SMTP_PORT = 25

@app.route(SERVICE_URL, methods=['POST'])
def send_email():
    recipient = request.form.get('recipient')
    subject = request.form.get('subject')
    content = request.form.get('content')

    if not recipient or not subject or not content:
        return jsonify({'error': 'Missing required fields'}), 400

    msg = MIMEText(content)
    msg['Subject'] = subject
    msg['From'] = SENDER_NAME + ' <' + SENDER_ADDRESS + '>'
    msg['To'] = recipient
    msg.add_header('Content-Type', 'text/html')

    try:
        with smtplib.SMTP(SMTP_SERVER, SMTP_PORT) as server:
            server.sendmail(SENDER_ADDRESS, [msg['To']], msg.as_string())
        return jsonify({'success': 'Email sent successfully'}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=41567)