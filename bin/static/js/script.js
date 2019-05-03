// Ajax Error Handling
function sendRedirect(status) {
	
	switch(status) {
		
		// 인증 만료
		case 0 :
		case 500 :
			alert("인증 시간이 만료되었습니다. 재로그인 하시기 바랍니다.");
			location.href = "/member/login";
			break;
		
		default :
			alert("문제가 발생하였습니다. 재로그인 하시기 바랍니다.");
			location.href = "/member/login";
			break;
	}
	
}

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

// 날짜 입력 키 이벤트 확인
function checkKeyEvent(key) {
	
	// Key 
	// [8 : backSpace] [9 : tap], [37 : 왼쪽 방향키], [39 : 오른쪽 방향키], [46 : delete], [48 ~ 57 :  0 ~ 9], [96 ~105 : 넘버패드]
	if(!(key == 8 || key == 9 || key == 37 || key == 39 || key == 46 || (key >= 48 && key <= 57) || (key >= 96 && key <= 105))) {
		return false;
	}
	return true;
}

