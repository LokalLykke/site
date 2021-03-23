var webpack = require('webpack')
const {merge} = require("webpack-merge")
var generated = require('./scalajs.webpack.config');

module.exports = merge(generated, {
  module : {
    rules : [
      {
        test : /\.css/i,
        use: ['style-loader', 'css-loader']
      },
      {
        test : /\.s[ac]ss$/i,
        use: ["style-loader", "css-loader", "sass-loader"]

      }
    ]
  }
})
