package Main;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static Main.ActionsWithDB.*;

public class Main {

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            OpenConnectionWithDB();
            botsApi.registerBot(new TeleTubeBot());
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }

        System.out.println("TeleTubeBot successfully started!");
    }

}
