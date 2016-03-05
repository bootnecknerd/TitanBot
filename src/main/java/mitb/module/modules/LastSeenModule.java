package mitb.module.modules;

import com.google.common.base.Joiner;
import mitb.TitanBot;
import mitb.event.EventHandler;
import mitb.event.Listener;
import mitb.event.events.CommandEvent;
import mitb.event.events.MessageEvent;
import mitb.module.CommandModule;
import org.ocpsoft.prettytime.PrettyTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Checks when a user was last seen speaking
 */
public class LastSeenModule extends CommandModule {

    @Override
    public String[] getCommands() {
        return new String[]{"seen", "lastseen"};
    }

    /**
     * Check when the specified person last spoke and return with a response.
     */
    @Override
    public void onCommand(CommandEvent commandEvent) {
        if(commandEvent.getArgs().length == 0) {
            return;
        }

        String nick = Joiner.on(" ").join(commandEvent.getArgs());

        try {
            PreparedStatement statement = TitanBot.databaseConnection.prepareStatement(
                    "SELECT seen FROM seen WHERE nick LIKE ?"
            );
            statement.setString(1, nick);
            ResultSet resultSet = statement.executeQuery();
            PrettyTime prettyTime = new PrettyTime();
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultSet.getString("seen"));
            String pretty = prettyTime.format(date);

            TitanBot.sendReply(commandEvent.getOriginalEvent(), "I last saw " + nick + " " + pretty + ".");
        } catch(Exception e) {
            e.printStackTrace();
            TitanBot.sendReply(commandEvent.getOriginalEvent(), "I have never seen " + nick + ".");
        }
    }

    @Override
    public void register() {
        EventHandler.register(this);
    }

    /**
     * Update the last time a user has spoken every time they say something.
     *
     * @param event message event when they have said something
     */
    @Listener
    public void onChatter(MessageEvent event) {
        try {
            PreparedStatement statement = TitanBot.databaseConnection.prepareStatement(
                    "INSERT OR REPLACE INTO seen (id, nick, login, seen) VALUES ((SELECT id FROM seen WHERE login = ?), ?, ?, datetime())"
            );
            statement.setString(1, event.getOriginalEvent().getUser().getLogin());
            statement.setString(2, event.getOriginalEvent().getUser().getNick());
            statement.setString(3, event.getOriginalEvent().getUser().getLogin());
            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}