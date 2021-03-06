package org.musicbox;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import javax.management.InstanceAlreadyExistsException;
import javax.security.auth.login.LoginException;

import org.musicbox.commands.MusicCommands;
import org.musicbox.config.DefaultConfig;
import org.musicbox.core.managers.AudioManager;
import org.musicbox.core.managers.CommandManager;
import org.musicbox.core.managers.GuildManager;
import org.musicbox.core.managers.LanguageManager;
import org.musicbox.core.managers.ListenerManager;
import org.musicbox.core.module.Modules;
import org.musicbox.listeners.CommandListener;
import org.musicbox.listeners.PresenceListener;
import org.musicbox.listeners.InspectorListener;
import org.musicbox.models.FailHandler;
import org.musicbox.modules.paginator.PaginatorModule;
import org.musicbox.modules.youtube.YoutubeSearchModule;
import org.playground.Playground;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.config.ThreadingConfig;
import okhttp3.OkHttpClient;

public class MusicBox {
   
   private static Logger logger = LoggerFactory.getLogger(MusicBox.class);
   private static MusicBox musicBox;

   private final ShardManager shardManager;

   protected MusicBox() throws LoginException, InstanceAlreadyExistsException {
      
      if (musicBox != null)
         throw new InstanceAlreadyExistsException("MusicBox instance already exists.");

      DefaultConfig.setup("musicbox-app");

      if(DefaultConfig.ENABLE_PLAYGROUND) {
         Playground.setupPlayground();
      }

      /* setting up isolated modules */
      Modules.setup();
      Modules.registerAll(YoutubeSearchModule.class, 
            PaginatorModule.class);
      
      /* setting up application managers */
      AudioManager.setup();
      LanguageManager.setup();
      GuildManager.setup();
      FailHandler.setup();

      CommandManager.setup();
      CommandManager.getCommandManager().handle(MusicCommands.class);

      ListenerManager.setup();

      ListenerManager.register(
            new InspectorListener(),
            new CommandListener(),
            new PresenceListener());

      RestAction.setDefaultFailure(null);

      EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES
            );

      Collection<CacheFlag> cacheFlags = Arrays.asList(
            CacheFlag.MEMBER_OVERRIDES,
            CacheFlag.ROLE_TAGS,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ACTIVITY,
            CacheFlag.EMOTE
            );

      /* setting up JDA */
      shardManager = DefaultShardManagerBuilder.create(
            DefaultConfig.TOKEN, intents)
            .disableCache(cacheFlags)
            .setGatewayEncoding(GatewayEncoding.ETF)
            .setChunkingFilter(ChunkingFilter.NONE)
            .setMemberCachePolicy(MemberCachePolicy.VOICE)
            .addEventListeners(ListenerManager.getAllListeners())
            .setRawEventsEnabled(true)
            .setBulkDeleteSplittingEnabled(false)
            .setEventPool(ThreadingConfig.newScheduler(1, () -> "MusicBox", "EventPool"), true)
            .setHttpClient(new OkHttpClient())
            .build();

      logger.info("MusicBox is ready to be used.");
      musicBox = this;
   }

   public ShardManager getShardManager() {
      return shardManager;
   }

   public static MusicBox getMusicBox() {
      return musicBox;
   }

}
