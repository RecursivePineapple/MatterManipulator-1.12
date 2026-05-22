package matter_manipulator.core.context;

import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceStack;

public interface FeedbackContext {

    void pushMessageContext(Localized context);
    void popMessageContext();

    void warn(Localized message);

    void error(Localized message);

    /// Emits a warning for the current block that a stack could not be extracted. To prevent spam, these are grouped by
    /// the resource identity and their amounts are summed. Each resource type will only print one message per build
    /// tick.
    void extractionFailure(ResourceStack stack);
}
