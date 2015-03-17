var context = require.context('./src/main/jsx', true, /-test\.jsx?$/);
context.keys().forEach(context);
