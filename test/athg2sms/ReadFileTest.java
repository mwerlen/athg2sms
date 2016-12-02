package athg2sms;

import android.test.mock.MockContext;

import org.junit.Assert;
import org.junit.Test;
import org.toilelibre.libe.athg2sms.androidstuff.ContextHolder;
import org.toilelibre.libe.athg2sms.business.convert.ConvertException;
import org.toilelibre.libe.athg2sms.business.convert.ConvertListener;
import org.toilelibre.libe.athg2sms.business.convert.Converter;
import org.toilelibre.libe.athg2sms.business.pattern.BuiltInFormatName;
import org.toilelibre.libe.athg2sms.business.pattern.FormatSettings;
import org.toilelibre.libe.athg2sms.business.sms.Sms;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

public class ReadFileTest {
    private int                       messagesInserted = 0;
    private List<Sms> messages = new ArrayList<>();

    private final ConvertListener convertListener  = new ConvertListener () {

        @Override
        public ConvertListener bind() {
            return null;
        }

        public int delete (final URI uriDelete, final String where, final String [] strings) {
            return 0;
        }

        public void displayInserted (final int inserted, final int dupl) {

        }

        public void end () {

        }

        public void insert (final URI uri, final Sms sms) {
            System.out.println (sms.getValues());
            messages.add (sms);
            ReadFileTest.this.messagesInserted++;
        }

        public void sayIPrepareTheList (final int size) {
        }

        public void setMax (final int nb2) {
            messages = new ArrayList<>(nb2);
            ReadFileTest.this.messagesInserted = 0;
        }

        public void updateProgress (final int i2, final int nb2) {
        }

    };

    @Test
    public void csvSms () throws URISyntaxException {
        // "\"Created\",\"Number\",\"Sender Name\",\"Text\",\"Folder\"\n"
        this.testString ("\"2016-04-19 01:04:34\",\"VM-FCHRGE\",\"\",\"Dear customer, You have made a Debit\",\"INBOX\"\n" + "\"2016-04-19 17:24:11\",\"ID-IDEA\",\"\",\"UR BSNL a/c Topup with Rs. 10 by 2222\",\"INBOX\"\n", BuiltInFormatName.DateAndAddressAndBodyAndINBOX, false);
        Assert.assertEquals (2, this.messagesInserted);
    }

    @Test(expected=ConvertException.class)
    public void empty () throws URISyntaxException {
        this.testString ("", BuiltInFormatName.NokiaCsv, true);
    }

    @Test
    public void indianGuy () throws URISyntaxException {
        this.testFile ("athg2sms/DE.csv", BuiltInFormatName.DateAndFromAndAddressAndbody, false);
    }

    @Test
    public void aleTxt () throws URISyntaxException {
        this.testFile ("athg2sms/ale.txt", BuiltInFormatName.NokiaCsvWithCommas, false);
    }

    @Test
    public void otherOtherNokia () throws URISyntaxException {
        this.testFile ("athg2sms/nokia.csv", BuiltInFormatName.NokiaSuite, false);
    }

    @Test
    public void lumia () throws URISyntaxException {
        this.testFile ("athg2sms/sms.vmsg", BuiltInFormatName.LumiaVmg, false);
    }

    @Test
    public void lionel () throws URISyntaxException {
        this.testFile ("/mnt/data/lionel/Documents/misc/NouvelOrdi/Msgs5200.csv", BuiltInFormatName.NokiaCsv, false);
    }

    @Test
    public void yetAnotherTest () throws URISyntaxException {
        this.testFile ("/mnt/data/lionel/Documents/misc/NouvelOrdi/test.csv", BuiltInFormatName.NokiaCsv, false);
    }

    @Test
    public void checkingTheDateFormat () throws URISyntaxException {
        this.testString ("sms;submit;\"0000000\";\"\";\"\";\"2010.01.11 16:05\";\"\";\"Bonjour Ca va ?\"\n", BuiltInFormatName.NokiaCsv, false);
        Date d = new Date ((Long)messages.get (0).getDate());
        Calendar c = new GregorianCalendar ();
        c.setTime (d);
        int hourOfDay = c.get (Calendar.HOUR_OF_DAY);
        int minutes = c.get (Calendar.MINUTE);
        Assert.assertTrue (hourOfDay == 16);
        Assert.assertTrue (minutes == 05);
    }

    @Test
    public void checkingTheDateFormat2 () throws URISyntaxException {
        this.testString ("sms;submit;\"0000000\";\"\";\"\";\"2010.01.11 12:04\";\"\";\"C'est l'heure de manger\"\n", BuiltInFormatName.NokiaCsv, false);
        Date d = new Date ((Long)messages.get (0).getDate());
        Calendar c = new GregorianCalendar ();
        c.setTime (d);
        int hourOfDay = c.get (Calendar.HOUR_OF_DAY);
        int minutes = c.get (Calendar.MINUTE);
        Assert.assertTrue (hourOfDay == 12);
        Assert.assertTrue (minutes == 04);
    }

    @Test
    public void unknownSmsFormat () throws URISyntaxException {
        this.testString ("\"+33682864563\",\"2015-07-10 21:53\",\"SMS\",\"0\",\"Bienvenue\"\n", BuiltInFormatName.UnknownSmsFormat1, true);
        Assert.assertEquals (1, this.messagesInserted);
    }

    @Test
    public void nokiaCsv () throws URISyntaxException {
        this.testString ("sms;deliver;\"+33612345678\";\"\";\"\";\"2016.03.22 15:46\";\"\";\"First message\"\n" + "sms;submit;\"\";\"+33612345678\";\"\";\"2016.03.22 15:48\";\"\";\"Answer to the first message\"", BuiltInFormatName.NokiaCsv, true);
        Assert.assertEquals (2, this.messagesInserted);
    }

    @Test
    public void nokiaCsvWithCR () throws URISyntaxException {
        this.testString (
                "\"sms\";\"submit\";\"+498537215678\";\"\";\"\";\"2016.04.14 11:58\";\"\";\"How are you doing?\"\n"
                        + "\"sms\";\"submit\";\"00434566400787\";\"\";\"\";\"2016.04.10 10:43\";\"\";\"Neue Info OS129: Die aktuelle Abflugzeit ist jetzt voraussichtlich 10Apr 11:10. Wir bitten um Entschuldigung.\"",
                BuiltInFormatName.NokiaCsvWithQuotes, false);
        Assert.assertEquals (2, this.messagesInserted);
    }

    @Test
    public void nokiaSuite () throws URISyntaxException {

        //"sms","$(folder)",(?:"",)?"$(address)",(?:"",)?"","$(dateyyyy.MM.dd hh:mm)","","$(body)"
        this.testString (
                "\"sms\",\"READ,RECEIVED\",\"+33654321009\",\"\",\"\",\"2015.04.19 12:23\",\"\",\"Here is a received message\"\n" +
                        "\"sms\",\"SENT\",\"\",\"+33634567811\",\"\",\"2015.04.20 18:49\",\"\",\"Here is a sent message\"\n",
                BuiltInFormatName.NokiaSuite, false);
        Assert.assertEquals (2, this.messagesInserted);
    }

    @Test
    public void loremIpsum () throws URISyntaxException {
        this.testString ("-545061504,Fri Feb 19 03:18:04 EST 2010,Thu Feb 18 16:18:10 EST 2010,false,+61422798642,\"Lorem ipsumRecu\"\n" + "-491825428,Fri Feb 19 07:05:26 EST 2010,Fri Feb 19 07:05:26 EST 2010,true,+61432988391,\"Lorem ipsumSent\"", BuiltInFormatName.BlackberryCsv, false);
        Assert.assertEquals (2, this.messagesInserted);
    }

    @Test
    public void vmg () throws URISyntaxException {
        this.testFile ("/mnt/data/lionel/Documents/workspace/athg2sms/test/athg2sms/test.vmg", BuiltInFormatName.NokiaVmgInbox, false);
        Assert.assertEquals (1, this.messagesInserted);
    }

    public void testFile (final String classpathFile, final BuiltInFormatName conversionSet, final boolean shouldBeEmpty) throws URISyntaxException {
        // Given
        final Converter convertV4 = new Converter();
        final URL url = ReadFileTest.class.getClassLoader ().getResource (classpathFile);
        final String content;
        try {
            final Scanner scan = new Scanner (url == null ? new File (classpathFile) : new File (url.toURI ()));
            scan.useDelimiter ("\\Z");
            content = scan.next ();
            scan.close ();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException (e);
        }

        // When
        convertV4.convertNow(FormatSettings.getInstance().getFormats().get(conversionSet.getValue()),
                content, this.convertListener, null, new ContextHolder<>(null),
                null, null);

        // then
        if (!shouldBeEmpty) {
            Assert.assertTrue (this.messagesInserted > 0);
        }
    }

    public void testString (final String content, final BuiltInFormatName conversionSet, final boolean shouldBeEmpty) throws URISyntaxException {
        // Given
        final Converter convertV4 = new Converter();

        // When
        convertV4.convertNow(FormatSettings.getInstance().getFormats().get(conversionSet.getValue()),
                content, this.convertListener, null, new ContextHolder<>(null),
                null, null);

        // then
        if (!shouldBeEmpty) {
            Assert.assertTrue (this.messagesInserted > 0);
        }
    }

    @Test
    public void regexTest () {
        Assert.assertTrue (java.util.regex.Pattern.compile("[\r\n\t]*((?:[^;])*);((?:[^;])*);((?:[^;])*);\"\";\"((?:[^\"]|\"\")*)\"[\r\n\t]+").matcher (
                "12/26/2015 6:22:03 AM;from;+654631231157;\"\";\"na go SONA kno problem hbe na ar oke phn krba go\"\n" +
                        "12/26/2015 6:17:58 AM;from;+654631231157;\"\";\"blchi j tmr sottie kno problem hbe na to go JAAN? ar sei phn gulo deini go ar ki blche go SONAAA?\"\n" +
                        "12/26/2015 6:14:29 AM;from;+654631231157;\"\";\"thik hai\"\n" +
                        "12/26/2015 6:10:48 AM;from;+654631231157;\"\";\"toainaki ha ha ha ha ha\"\n" +
                        "12/26/2015 5:25:57 AM;from;+654631231157;\"\";\"thikache go JAAN tale mongolbarei jabo go\"\n" +
                        "12/26/2015 4:27:18 AM;from;+654631231157;\"\";\"na go SONATA mongolbare hbe na go karn maa ager dn mane mongolbare fanudr bari jabe go tale?\"\n" +
                        "12/26/2015 4:18:23 AM;from;+654631231157;\"\";\"ami blle nebi na krbe na go jabei tale porer sombare jabe go school theke?\"\n" +
                        "12/26/2015 4:11:36 AM;from;+654631231157;\"\";\"kintu mayer operation krar por ki ar jaua hbe go? karn tkhn to amke sb kaj krte hbe abr jdi fanu ase ta o to jaua late uthie dibe go JAAN ki je kri chhai\"\n" +
                        "12/26/2015 4:01:11 AM;from;+654631231157;\"\";\"tau ktodner mddhe eta blen go\"\n" +
                        "12/").find ());
    }
}
