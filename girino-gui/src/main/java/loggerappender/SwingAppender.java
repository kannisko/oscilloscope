package loggerappender;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

/**
 * Created by Pawel.Piotrowski on 2015-12-23.
 */
public class SwingAppender<E> extends AppenderBase<E> {
    SwingAppenderUI swingAppenderUI = SwingAppenderUI.getInstance();

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    String pattern = "%msg";


    protected Layout<E> layout;

    public void start(){
        super.start();
        layout = buildLayout();

    }
    public Layout buildLayout() {
        PatternLayout layout = new PatternLayout();
        layout.setPattern(pattern);
        layout.setContext(getContext());
        layout.start();
        return layout;
    }


    @Override
    protected void append(E eventObject) {
        String msg = layout.doLayout(eventObject);
        if(msg == null) {
            return;
        }
        swingAppenderUI.doLog(msg);
    }

}
