package com.pragbits.stash;

import com.atlassian.stash.repository.Repository;
import javax.annotation.Nonnull;

public interface ChatworkSettingsService {

    @Nonnull
    ChatworkSettings getChatworkSettings(@Nonnull Repository repository);

    @Nonnull
    ChatworkSettings setChatworkSettings(@Nonnull Repository repository, @Nonnull ChatworkSettings settings);

}
