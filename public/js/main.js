var tableClass = 'table table-bordered table-hover table-sm w-auto'
var envChangeHandlers = []


$(document).ready(function() {
  $('#env-text').text(currentEnv)
  $('a[typ="env-drop"]').click(function(){
    let newEnv = $(this).attr('value')
    if(newEnv != currentEnv) {
      $('#env-text').text(newEnv)
      currentEnv = newEnv
      document.cookie = envCookieName + '=' + currentEnv + '; max-age=' + (60*60*24*30)

      $.each(envChangeHandlers, function(idx, handler) {
        handler()
      })
    }
  })
})


function clearTablesAbove(levNo) {
  $('div[is-table-insert-div="true"]').each(function(){
    if(levNo == null) $(this).empty()
    else {
      let lev = parseInt($(this).attr('level'))
      if(lev>= levNo) {
        $(this).empty()
      }
    }
  })
}


function insertTable(table, levNo, tabClass) {
  let tbCls = tabClass == null ? tableClass : tabClass
  clearTablesAbove(levNo)
  if(tableData.length > levNo) {
    tableData.length = levNo
  }
  tableData[levNo] = table
  tableBuilder.buildLevel(levNo, tableData, tbCls)

}
