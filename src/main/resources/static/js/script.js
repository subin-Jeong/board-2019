// 숫자 0 자동채우기
function zeroPad(n, width) {
	n = n + "";
	return n.length >= width ? n : new Array(width - n.length + 1).join("0") + n;
}

// 숫자 형식 출력
Number.prototype.format = function(){
    if(this==0) return 0;
 
    var reg = /(^[+-]?\d+)(\d{3})/;
    var n = (this + '');
 
    while (reg.test(n)) n = n.replace(reg, '$1' + ',' + '$2');
 
    return n;
};
