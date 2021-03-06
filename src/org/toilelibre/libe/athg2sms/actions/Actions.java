package org.toilelibre.libe.athg2sms.actions;

import org.toilelibre.libe.athg2sms.R;
import org.toilelibre.libe.athg2sms.androidstuff.api.activities.ContextHolder;
import org.toilelibre.libe.athg2sms.androidstuff.api.activities.HandlerHolder;
import org.toilelibre.libe.athg2sms.androidstuff.api.storage.FileRetriever;
import org.toilelibre.libe.athg2sms.androidstuff.api.storage.SharedPreferencesHolder;
import org.toilelibre.libe.athg2sms.androidstuff.interactions.ProcessRealTimeFeedback;
import org.toilelibre.libe.athg2sms.androidstuff.sms.SmsDeleter;
import org.toilelibre.libe.athg2sms.androidstuff.sms.SmsFinder;
import org.toilelibre.libe.athg2sms.androidstuff.sms.SmsInserter;
import org.toilelibre.libe.athg2sms.business.convert.ConvertException;
import org.toilelibre.libe.athg2sms.business.convert.ConvertFormatGuesser;
import org.toilelibre.libe.athg2sms.business.convert.Converter;
import org.toilelibre.libe.athg2sms.business.export.Exporter;
import org.toilelibre.libe.athg2sms.business.pattern.Format;
import org.toilelibre.libe.athg2sms.business.pattern.FormatSettings;
import org.toilelibre.libe.athg2sms.business.preferences.AppPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.locks.Condition;

public class Actions {

    public void saveDefaultSmsApp (SharedPreferencesHolder<?> preferencesHolder, String smsPackage) {
        new AppPreferences(preferencesHolder).saveDefaultSmsApp (smsPackage);
    }

    public String getDefaultSmsApp(SharedPreferencesHolder<?> preferences) {
        return new AppPreferences(preferences).getDefaultSmsApp ();
    }

    public String guessNow(String content) throws FileNotFoundException {
        return new ConvertFormatGuesser().guessNow(content);
    }

    public String getFailedGuessDetailsFor(String content, String pattern) {
        return new ConvertFormatGuesser().getFailedGuessDetailsFor(content, pattern);
    }


    public String[] getAllFormats() {
        return FormatSettings.getInstance().getFormats().keySet().toArray(new String [FormatSettings.getInstance().getFormats().size ()]);
    }

    public void runConversionNow(ContextHolder<?> contextHolder, Runnable done, ErrorHandler errorHandler, Runnable abort, String filename, String pattern, Condition stopMonitor) {
        final Converter thisConverter = new Converter();
        ProcessRealTimeFeedback convertListener = ProcessRealTimeFeedback.getInstance();
        final String content = this.getContentFromFileName (contextHolder, filename);
        if (content == null || convertListener == null) {
            return;
        }
        Converter.ConversionResult result;
        try {
            result = thisConverter.convertNow(FormatSettings.getInstance().getFormats().get(
                    pattern), content,
                    convertListener, new HandlerHolder<Object>(convertListener.getHandler()),
                    contextHolder, new SmsInserter(), new SmsDeleter(), stopMonitor);
        } catch (ConvertException ce) {
            errorHandler.run(ce);
            return;
        } finally {
            ProcessRealTimeFeedback.unbind();
        }
        if (result.getTotal() > 0 && result.getInserted() == 0)
            errorHandler.run(new ParseException(contextHolder.getString(R.string.nosmsimported), 0));
        else if (result.getTotal () > 0) {
            done.run();
        } else {
            abort.run();
        }
    }


    public String getContentFromFileName (ContextHolder<?> context, String filename) {
        try {
            return FileRetriever.getFile (context, filename);
        } catch (FileNotFoundException e) {
            ProcessRealTimeFeedback.unbind();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public void exportNow(Object context, File tempFile, SmsFinder smsFinder, Runnable afterExport, String pattern, Condition stopMonitor) {

        final ProcessRealTimeFeedback convertListener = ProcessRealTimeFeedback.getInstance();
        final ConvertListener uncastedConvertListener = (ConvertListener) convertListener;
        final String result = new Exporter().export(smsFinder, new ContextHolder<Object>(context),
                new HandlerHolder<Object>(convertListener.getHandler()), pattern, uncastedConvertListener, stopMonitor);

        if (result == null) {
            afterExport.run();
            return;
        }
        try {
            FileWriter fw = new FileWriter(tempFile);
            fw.write(result);
            fw.close();

            afterExport.run();
        } catch (IOException e) {
            throw new ConvertException("Exporting the sms did not work because of a problem with the storage", e);
        }
    }

    public void saveFormats(SharedPreferencesHolder<?> preferences) {
        new AppPreferences(preferences).saveFormats(FormatSettings.getInstance().getFormats());
    }

    public void removeFormat(String pattern) {
        FormatSettings.getInstance().getFormats().remove(pattern);
    }

    public void addOrChangeFormat(String patternName, String pattern, String exportFormat, String inbox, String sent) {
        Format format = new Format(patternName, pattern, exportFormat.length() == 0 ? pattern : exportFormat, inbox, sent);
        FormatSettings.getInstance().addOrChangeFormats(format);
    }

    public Format.FormatRegexRepresentation getFormatRegex(String pattern) {
        return FormatSettings.getInstance().getFormats().get(pattern).getRegex();
    }
}
