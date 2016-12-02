package org.toilelibre.libe.athg2sms.business.export;


import org.toilelibre.libe.athg2sms.androidstuff.api.activities.ContextHolder;
import org.toilelibre.libe.athg2sms.androidstuff.api.activities.HandlerHolder;
import org.toilelibre.libe.athg2sms.androidstuff.sms.SmsFinder;
import org.toilelibre.libe.athg2sms.business.convert.ConvertListener;
import org.toilelibre.libe.athg2sms.business.sms.Sms;

import java.util.List;
import java.util.Map;

public class Exporter {

    public String export(final ContextHolder<?> context, final HandlerHolder<?> handler, final String patternName, final ConvertListener convertListener) {
        final StringBuilder result = new StringBuilder();
        final MessageMapper messageMapper = new MessageMapper();
        final List<Map<String, Object>> list = new SmsFinder().pickThemAll(context, handler, convertListener);

        for (int i = 0; i < list.size() ; i++) {
            final int thisIndex = i;
            handler.postForHandler(new Runnable() {
                @Override
                public void run() {
                   convertListener.updateProgress(thisIndex, list.size());
                }
            });
            Sms sms = new Sms(list.get(i));
            result.append(messageMapper.convert(sms, patternName));
        }
        return result.toString();
    }
}
