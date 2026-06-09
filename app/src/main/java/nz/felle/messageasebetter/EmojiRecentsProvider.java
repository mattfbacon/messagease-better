package nz.felle.messageasebetter;

import java.util.Arrays;
import java.util.List;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.emoji2.emojipicker.RecentEmojiAsyncProvider;

public class EmojiRecentsProvider implements RecentEmojiAsyncProvider {
	public EmojiRecentsProvider() {}

	public ListenableFuture<List<String>> getRecentEmojiListAsync() {
		final @NonNull List<String> ret = Arrays.asList("👍", "🙂", "👌", "🤔", "👀", "🤷‍♀️", "💯", "🍴", "❓");

		return Futures.immediateFuture(ret);
	}

	public void recordSelection(final @NonNull String _emoji) {}
}
