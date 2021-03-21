package red.felnull.imp.packet;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import red.felnull.imp.IamMusicPlayer;
import red.felnull.imp.client.handler.MusicRingMessageHandler;
import red.felnull.imp.client.handler.MusicRingUpdateMessageHandlerr;
import red.felnull.imp.client.handler.WorldMusicSendByteMessageHandler;
import red.felnull.imp.handler.*;

public class PacketHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(IamMusicPlayer.MODID, IamMusicPlayer.MODID + "_channel")).clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();
    private static int integer = -1;

    private static int next() {
        integer++;
        return integer;
    }

    public static void init() {
        //プレイリスト作成リクエスト
        INSTANCE.registerMessage(next(), PlayListCreateRequestMessage.class, PlayListCreateRequestMessage::encodeMessege, PlayListCreateRequestMessage::decodeMessege, PlayListCreateRequestMessageHandler::reversiveMessage);
        //音楽作成リクエスト
        INSTANCE.registerMessage(next(), PlayMusicCreateRequestMessage.class, PlayMusicCreateRequestMessage::encodeMessege, PlayMusicCreateRequestMessage::decodeMessege, PlayMusicCreateRequestMessageHandler::reversiveMessage);
        //ワールド音楽ファイルのデータ送信
        INSTANCE.registerMessage(next(), WorldMusicSendByteMessage.class, WorldMusicSendByteMessage::encodeMessege, WorldMusicSendByteMessage::decodeMessege, WorldMusicSendByteMessageHandler::reversiveMessage);
        //ワールドの音源をクライアントに伝える
        INSTANCE.registerMessage(next(), MusicRingMessage.class, MusicRingMessage::encodeMessege, MusicRingMessage::decodeMessege, MusicRingMessageHandler::reversiveMessage);
        //ワールドの音源をクライアントに同期
        INSTANCE.registerMessage(next(), MusicRingUpdateMessage.class, MusicRingUpdateMessage::encodeMessege, MusicRingUpdateMessage::decodeMessege, MusicRingUpdateMessageHandlerr::reversiveMessage);
        //プレイリスト変更リクエスト
        INSTANCE.registerMessage(next(), PlayListChangeRequestMessage.class, PlayListChangeRequestMessage::encodeMessege, PlayListChangeRequestMessage::decodeMessege, PlayListChangeRequestMessageHandler::reversiveMessage);
        //プレイリスト削除リクエスト
        INSTANCE.registerMessage(next(), PlayListRemoveRequestMessage.class, PlayListRemoveRequestMessage::encodeMessege, PlayListRemoveRequestMessage::decodeMessege, PlayListRemoveRequestMessageHandler::reversiveMessage);
        //音楽変更リクエスト
        INSTANCE.registerMessage(next(), PlayMusicChangeRequestMessage.class, PlayMusicChangeRequestMessage::encodeMessege, PlayMusicChangeRequestMessage::decodeMessege, PlayMusicChangeRequestMessageHandler::reversiveMessage);
        //音楽削除リクエスト
        INSTANCE.registerMessage(next(), PlayMusicRemoveRequestMessage.class, PlayMusicRemoveRequestMessage::encodeMessege, PlayMusicRemoveRequestMessage::decodeMessege, PlayMusicRemoveRequestMessageHandler::reversiveMessage);
    }
}
