/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.notifications;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.model.BuildListener;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.groupon.jenkins.util.LogUtils.debug;

public abstract class EmailNotifierBase extends PostBuildNotifier {
    private static final int NUM_RETRIES = 1;
    private static final Logger LOGGER = Logger.getLogger(EmailNotifierBase.class.getName());

    public EmailNotifierBase(String name) {
        super(name);
    }

    @Override
    protected Type getType() {
        return PostBuildNotifier.Type.FAILURE_AND_RECOVERY;
    }

    @Override
    public boolean notify(DynamicBuild build, BuildListener listener) {
        debug(listener.getLogger(), "Sending  email notifications");
        if (needsEmail(build, listener)) {
            sendMail(listener, build);
        }
        return true;
    }

    private boolean sendMail(BuildListener listener, DynamicBuild build) {
        try {
            MimeMessage msg = createEmail(build, listener);
            Address[] allRecipients = msg.getAllRecipients();
            int retries = 0;
            if (allRecipients != null) {
                StringBuilder buf = new StringBuilder("Sending email to:");
                for (Address a : allRecipients) {
                    buf.append(' ').append(a);
                }
                listener.getLogger().println(buf);
                while (true) {
                    try {
                        Transport.send(msg);
                        break;
                    } catch (SendFailedException e) {
                        //@formatter:off
                        if (e.getNextException() != null
                            && ((e.getNextException() instanceof SocketException)
                            || (e.getNextException() instanceof ConnectException))) {
                            //@formatter:on
                            listener.getLogger().println("Socket error sending email, retrying once more in 10 seconds...");
                            Thread.sleep(10000);
                        } else {
                            Address[] addresses = e.getValidSentAddresses();
                            printToConsoleIfSuccess(listener, addresses);
                            addresses = e.getValidUnsentAddresses();
                            if (addresses != null && addresses.length > 0) {
                                buf = new StringBuilder("Error sending to the following VALID addresses:");
                                for (Address a : addresses) {
                                    buf.append(' ').append(a);
                                }
                                listener.getLogger().println(buf);
                            }
                            addresses = e.getInvalidAddresses();
                            if (addresses != null && addresses.length > 0) {
                                buf = new StringBuilder("Error sending to the following INVALID addresses:");
                                for (Address a : addresses) {
                                    buf.append(' ').append(a);
                                }
                                listener.getLogger().println(buf);
                            }
                            debug(listener.getLogger(), "SendFailedException message: " + e.getMessage());
                            break;
                        }
                    }
                    retries++;
                    if (retries > NUM_RETRIES) {
                        listener.getLogger().println("Failed after second try sending email");
                        break;
                    }
                }

            } else {
                listener.getLogger().println("Email sending was cancelled by user script.");
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not send email.", e);
            e.printStackTrace(listener.error("Could not send email as a part of the post-build publishers."));
        }

        debug(listener.getLogger(), "Some error occured trying to send the email...check the Jenkins log");
        return false;
    }

    private void printToConsoleIfSuccess(BuildListener listener, Address[] addresses) {
        if (addresses != null && addresses.length > 0) {
            StringBuilder sucessBuf = new StringBuilder("Successfully sent to the following addresses:");
            for (Address a : addresses) {
                sucessBuf.append(' ').append(a);
            }
            listener.getLogger().println(sucessBuf);
        }
    }

    public boolean needsEmail(DynamicBuild build, BuildListener listener) {
        return true;
    }

    public MimeMessage createEmail(DynamicBuild build, BuildListener listener) throws AddressException, MessagingException {
        List<InternetAddress> to = getToEmailAddress(build, listener);

        String from = SetupConfig.get().getFromEmailAddress();
        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(from));

        message.addRecipients(Message.RecipientType.TO, to.toArray(new InternetAddress[to.size()]));

        String subject = getNotificationMessage(build, listener);
        message.setSubject(subject);
        message.setText("Link to the build " + build.getAbsoluteUrl());
        return message;
    }

    protected List<InternetAddress> getToEmailAddress(DynamicBuild build, BuildListener listener) throws AddressException {
        List<InternetAddress> addresses = new ArrayList<InternetAddress>();
        List<String> emails = getNotificationEmails(build);
        if (emails != null) {
            for (String email : emails) {
                if (email != null) {
                    addresses.add(new InternetAddress(email));
                }
            }
        }
        return addresses;
    }

    protected abstract List<String> getNotificationEmails(DynamicBuild build);

}
