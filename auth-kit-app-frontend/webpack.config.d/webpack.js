config.resolve.modules.push("../../processedResources/js/main");
config.resolve.conditionNames = ['import', 'require', 'default'];

const path = require('path');

if (config.devServer) {
    config.devServer.hot = true;
    config.devServer.open = false;
    config.devServer.port = 3002;
    config.devServer.historyApiFallback = true;
    config.devServer.compress = false; // workaround for SSE
    config.devtool = 'eval-cheap-source-map';
    config.devServer.allowedHosts = "all";
} else {
    config.devtool = undefined;
}

config.output = {
    filename: (chunkData) => {
        return chunkData.chunk.name === 'main'
            ? "main.bundle.js"
            : "main.bundle-[name].js";
    },
    publicPath: "/static",
    library: "project",
    libraryTarget: "umd",
    globalObject: "this"
};
config.output.path = require('path').resolve(__dirname, "../../../dist/libs")

// disable bundle size warning
config.performance = {
    assetFilter: function (assetFilename) {
      return !assetFilename.endsWith('.js');
    },
};
