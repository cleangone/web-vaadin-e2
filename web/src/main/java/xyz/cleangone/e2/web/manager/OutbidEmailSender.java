package xyz.cleangone.e2.web.manager;

import xyz.cleangone.data.aws.dynamo.dao.CatalogItemDao;
import xyz.cleangone.data.aws.dynamo.dao.UserDao;
import xyz.cleangone.data.aws.dynamo.entity.bid.ItemBid;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.person.User;
import xyz.cleangone.message.EmailSender;

public class OutbidEmailSender implements Runnable
{
    private EmailSender emailSender = new EmailSender();

    private final ItemBid previousHighBid;
    private final SessionManager sessionMgr;

    public OutbidEmailSender(ItemBid previousHighBid, SessionManager sessionMgr)
    {
        this.previousHighBid = previousHighBid;
        this.sessionMgr = sessionMgr;
    }

    public void run()
    {
        System.out.println("Sending outbid email ");

        // todo - is it okay to use daos directly for background db retrieve?
        UserDao userDao = new UserDao();
        User user = userDao.getById(previousHighBid.getUserId());

        CatalogItemDao itemDao = new CatalogItemDao();
        CatalogItem item = itemDao.getById(previousHighBid.getItemId());

        if (user != null &&
            item != null &&
            user.getEmail() != null &&
            user.getEmailVerified())
        {
//        UserToken token = userMgr.createToken();
//        String link = sessionMgr.getUrl(MyUI.VERIFY_EMAIL_URL_PARAM, token);
            String subject = "Outbid Notification";

            String htmlBody = "<h1>You have been outbid</h1> " +
                "<p>Your bid of $" + previousHighBid.getMaxAmount() +
                " for " + item.getName() + " has been outbid.</p>";
//            "<p><a href='" + link + "'>" + link + "</a>";

            String textBody = "You have been outbid. " +
                "Your bid of $" + previousHighBid.getMaxAmount() +
                " for " + item.getName() + " has been outbid.";

            boolean emailSent = emailSender.sendEmail(user.getEmail(), subject, htmlBody, textBody);
        }
    }

}
