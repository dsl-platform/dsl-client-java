package com.dslplatform.client.json;

import java.io.IOException;
import java.io.Writer;

public interface JsonObject {
	void serialize(Writer writer, boolean minimal) throws IOException;
}
