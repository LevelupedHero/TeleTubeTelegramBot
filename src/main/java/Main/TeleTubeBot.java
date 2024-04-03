package Main;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TeleTubeBot extends TelegramLongPollingBot {

    Map<String, String> numberVideoLinkMap = new HashMap<String, String>();

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
        // Если update вызван сообщением пользователя
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
                        sendMsg.setText("Добро пожаловать! \u2728" +
                                "\nС этим ботом ты можешь смотреть \uD83C\uDF7F, оценивать \uD83D\uDC4D и делиться своими видео \uD83C\uDFAC с пользователями со всего мира \uD83C\uDF0D!" +
                                "\n\nВ боте доступны следующие команды:" +
                                "\n/show_all_videos - Выбрать видео из списка" +
                                "\n/upload_video - Загрузить свое видео");
                        break;
                    case ("/upload_video"):
                        sendTextMessage(usrId, "Отправьте ваше видео сюда в чат, название к видео укажите в описании.");
                        break;
                    case ("/show_all_videos"):
                        sendMsg = videoListWithButtonsMessage(usrId);
                        break;
                    default:
                        sendMsg.setText("Такой команды не существует. Лучше воспользуйтесь списком команд из Меню слева от поля ввода!");
                        break;
                }
            }
            // Если update не содержит текст, но содержит видео
            else if (msg.hasVideo()) {
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
        }
        // Если update вызван нажатием inline-кнопки
        else if (update.hasCallbackQuery()) {
            var usrId = update.getCallbackQuery().getFrom().getId();
            String callbackData = update.getCallbackQuery().getData();
            sendTeletubeVideo(usrId, numberVideoLinkMap.get(callbackData));

            // Отключаю анимацию загрузки у кнопки
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            try {
                execute(answerCallbackQuery);
            }
            catch (TelegramApiException e) {
                e.printStackTrace();
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

    public boolean downloadVideoIntoFileSystem(Message message) {
        try {
            // Загружаю видеофайл на сервер
            GetFile getVideoFile = new GetFile();
            getVideoFile.setFileId(message.getVideo().getFileId());
            var sourceFilePath = execute(getVideoFile).getFilePath();

            // Получаю ID пользователя
            int userDBId = ActionsWithDB.SelectOrInsertChatIntoDB(message.getFrom());

            String destinationFilePath = "C:\\Users\\komra\\IdeaProjects\\TeleTubeTelegramBot\\ExampleVideos\\" + userDBId + "\\" + generateRandomString(12) + ".mp4";
            downloadFile(sourceFilePath, new File(destinationFilePath));

            // Вношу запись о новом видео в базу данных
            ActionsWithDB.InsertNewVideoInfoIntoDB(message, destinationFilePath);

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Создает и возвращает список всех видео в БД в формате string
    public String makeVideoList() {
        String outputText = "";

        try {
            ResultSet videosSet = ActionsWithDB.SelectVideosFromDB();

            int videoNumber = 0;
            while (videosSet.next()) {
                videoNumber++;
                outputText += videoNumber + ". " + videosSet.getString("Name") + " • " + videosSet.getString("FirstName") + " \"" + videosSet.getString("Username") + "\" " + videosSet.getString("LastName") + "\n";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            return outputText;
        }

    }

    public SendMessage videoListWithButtonsMessage(Long chatId) {
        SendMessage videoListMsg = new SendMessage();
        videoListMsg.setChatId(chatId.toString());
        videoListMsg.setText(makeVideoList());

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        numberVideoLinkMap.clear();
        // Работаю с БД
        String outputText = "";
        try {
            ResultSet videosSet = ActionsWithDB.SelectVideosFromDB();

            int videoNumber = 0;
            while (videosSet.next()) {
                videoNumber++;

                // Создаю запись с названием видео в сообщении
                outputText += videoNumber + ") " + videosSet.getString("Name") + " • " + videosSet.getString("FirstName") + " \"" + videosSet.getString("Username") + "\" " + videosSet.getString("LastName") + "\n";

                // Добавляю ссылку на видео
                numberVideoLinkMap.put(String.valueOf(videoNumber), videosSet.getString("Link"));

                // Добавляю кнопку с номером видео к сообщению
                InlineKeyboardButton inlineBtn = new InlineKeyboardButton();
                inlineBtn.setText(String.valueOf(videoNumber));
                inlineBtn.setCallbackData(String.valueOf(videoNumber));

                List<InlineKeyboardButton> inlineRow = new ArrayList<>();
                inlineRow.add(inlineBtn);

                inlineRows.add(inlineRow);
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        inlineKeyboard.setKeyboard(inlineRows);
        videoListMsg.setReplyMarkup(inlineKeyboard);

        return videoListMsg;
    }

    public void sendTeletubeVideo(Long chatId, String filePath) {

        // Отправляю в чат сообщение о том, что видео готовиться к отправке
        sendTextMessage(chatId, "Видео загружается для просмотра, подождите немного...");

        // Загружаю видео с компьютера
        File file = new File(filePath);
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId.toString());
        sendVideo.setVideo(new InputFile(file, file.getName()));
        sendVideo.setSupportsStreaming(true);

        // Отправляю видео
        try {
            execute(sendVideo);
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateRandomString(int length) {
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
