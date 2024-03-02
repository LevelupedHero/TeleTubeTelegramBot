package Main;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Predicates.or;

public class TeleTubeBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "TeleTube_videobot";
    }

    @Override
    public String getBotToken() {
        return "6728795326:AAGkHywo5jGTx92N-zNVgVvYLOyWRuxb0sM";
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, содержит ли update сообщение
        if (update.hasMessage()) {
            CopyMessage copyMsg = new CopyMessage();

            Message msg = update.getMessage();                      // Сообщение update'а
            Long usrId = update.getMessage().getFrom().getId();     // ID чата - источника update

            copyMsg.setChatId(usrId.toString()); // Целевой чат - чат с оригинальным сообщением
            copyMsg.setFromChatId(usrId.toString()); // Чат-источник
            copyMsg.setMessageId(msg.getMessageId()); // Ресурс - сообщение, вызвавщее update

            try {
                execute(copyMsg);
            }
            catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
