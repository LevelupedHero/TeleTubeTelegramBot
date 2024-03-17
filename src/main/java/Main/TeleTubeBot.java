package Main;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
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
import java.util.Random;

public class TeleTubeBot extends TelegramLongPollingBot {

    // Метаданные
    boolean isWaitingVideo = false;

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
            var usr = update.getMessage().getFrom();
            Long usrId = usr.getId();     // ID чата-источника update

            SendMessage sendMsg = new SendMessage();
            sendMsg.setChatId(usrId.toString());

            System.out.println(update);

            if (msg.hasText()) {
                switch(msg.getText()) {
                    case ("/start"):
                        sendMsg.setText("Добро пожаловать! Выберите раздел в Меню.");
                        setMainMenuReplyKeyboard(sendMsg);
                        break;
                    case ("Случайное видео"):
                        sendTeletubeVideo(usrId, "C:\\Users\\komra\\IdeaProjects\\TeleTubeTelegramBot\\ExampleVideos\\5791410976-20897428.mp4");
                        break;
                    case ("/uploadVideo"):
                        sendTextMessage(usrId, "Отправьте ваше видео сюда в чат, название к видео укажите в описании.");
                        isWaitingVideo = true; // В isWaitingVideo станавливаю true, чтобы в следующем сообщении пользователя ждать видео
                        break;
//                    case ("/selectAll"):
//                        try {
//                            var results = ActionsWithDB.SelectAllFromDB();
//
//                            String text = new String();
//                            while(results.next()) {
//                                if (text != null) {
//                                    text += "\n";
//                                }
//                                text += results.getString(2) + " - " + results.getString(3) + " y.o.";
//                            }
//                            sendTextMessage(usrId, text);
//                        }
//                        catch (SQLException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    case ("/addMe"):
//                        ActionsWithDB.AddNewChatIntoDB(usr);
//                        break;
                    default:
                        sendMsg.setText("Такой команды не существует. Лучше воспользуйтесь списком команд из Меню слева от поля ввода!");
                        break;
                }
            }
            // Если update не содержит текст, но содержит видео
            else if (isWaitingVideo || msg.hasVideo()) {
                isWaitingVideo = false;
                sendTextMessage(usrId, "Загружаю видео на сервер, подождите...");

                // Выполняю скачивание пользовательского видео на сервер
                // И информирую пользователя о результате скачивания (успешно или нет)
                if (downloadVideoIntoFileSystem(update.getMessage())) {
                    sendTextMessage(usrId, "Ваше видео успешно загружено!");
                }
                else {
                    sendTextMessage(usrId, "Произошла ошибка при скачивании! Попробуйте повторить позже.");
                }

            }

            // Отправляю объект сообщения sendMsg, если ему были присвоены текст и ИД чата
            if (sendMsg.getText() != null && sendMsg.getChatId() != null) {
                try {
                    execute(sendMsg);
                }
                catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            if (isWaitingVideo) {
                isWaitingVideo = false;
                sendTextMessage(usrId, "Видео не обнаржено");
            }
        }
    }

    // Отправляет пользователю текстовое сообщение
    // Принимает на вход:   chatId - ID чата
    //                      txt - текст сообщения
    // Возвращает объект сообщения для возможности его дальнейшего удаления
    public Message sendTextMessage(Long chatId, String txt) {
        SendMessage sendMsg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(txt).build();

        Message sendOutMsg;
        try {
            sendOutMsg = execute(sendMsg);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);

        }
        return sendOutMsg;
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

    public boolean downloadVideoIntoFileSystem(Message message) {
        try {
            // Загружаю видеофайл на сервер
            GetFile getVideoFile = new GetFile();
            getVideoFile.setFileId(message.getVideo().getFileId());
            var sourceFilePath = execute(getVideoFile).getFilePath();

            // Получаю ID пользователя
            int userDBId = ActionsWithDB.SelectOrInsertChatIntoDB(message.getFrom());

            String destinationFilePath = "C:\\Users\\komra\\IdeaProjects\\TeleTubeTelegramBot\\ExampleVideos\\" + userDBId + "\\" + generateString(12) + ".mp4";
            downloadFile(sourceFilePath, new File(destinationFilePath));

            // Вношу запись о новом видео в базу данных
            ActionsWithDB.AddNewVideoInfoIntoDB(message, destinationFilePath);

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendTeletubeVideo(Long chatId, String filePath) {

        // Отправляю в чат сообщение о том, что видео готовиться к отправке
        Message temporaryMsg = sendTextMessage(chatId, "Видео загружается для просмотра, подождите немного...");

        // Загружаю видео с компьютера
        File file = new File(filePath);
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId.toString());
        sendVideo.setVideo(new InputFile(file, file.getName()));

//                sendVideo.setDuration();
//                sendVideo.setWidth();
//                sendVideo.setHeight();

        // Отправляю видео
        try {
            execute(sendVideo);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        // Удаляю из чата сообщение о подготовке видео
        DeleteMessage deleteMsg = new DeleteMessage();
        deleteMsg.setChatId(chatId.toString());
        deleteMsg.setMessageId(temporaryMsg.getMessageId());
        try {
            execute(deleteMsg);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        // Отправляю сообщение, содержащие название видео
        sendTextMessage(chatId, file.getName());
    }

    private static String generateString(int length) {
        Random rng = new Random();

        // Заполняю строку characters символами нижнего регистра
        String characters = "";
        for (char c = 'a'; c <= 'z'; c++) {
            characters += c;
        }

        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

}
