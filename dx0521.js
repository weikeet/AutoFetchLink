// 必须通过module.exports暴露接口
module.exports = {
  run: function() {
    console.log('Dynamic code executed!');
    $app.$def.data.dynamicConfig = {
      type: 'text',
      content: 'From Dynamic JS!'
    };
  }
};
