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
            Message msg = update.getMessage();                      // Сообщение update'а
            Long usrId = update.getMessage().getFrom().getId();     // ID чата - источника update

            SendMessage sendMsg = new SendMessage();
            sendMsg.setChatId(usrId.toString());

            if (msg.isCommand()) {
                switch(msg.getText()) {
                    case ("/start"):
                        sendMsg.setText("Добро пожаловать! Выберите раздел в Меню.");
                        setMainMenuReplyKeyboard(sendMsg);
                        break;
                    default:
                        sendMsg.setText("Такой команды не существует. Лучше воспользуйтесь списком команд из Меню слева от поля ввода!");
                        break;
                }
            }

            try {
                execute(sendMsg);
            }
            catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMainMenuReplyKeyboard(SendMessage sendMsg) {
        // Создаем клавиатуру и привязываем к сообщению
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMsg.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true); // Setting false

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Добавляю кнопки в список
        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Рекомендации"));
        keyboard.add(keyboardRow1);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add(new KeyboardButton("Топ популярных видео"));
        keyboard.add(keyboardRow2);

        KeyboardRow keyboardRow3 = new KeyboardRow();
        keyboardRow3.add(new KeyboardButton("Мои видео"));
        keyboard.add(keyboardRow3);

        KeyboardRow keyboardRow4 = new KeyboardRow();
        keyboardRow4.add(new KeyboardButton("Случайное видео"));
        keyboard.add(keyboardRow4);

        // Устанавливаем список клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

}
