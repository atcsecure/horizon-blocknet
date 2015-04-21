/**
 * @depends {nrs.js}
 */
var NRS = (function(NRS, $, undefined) {

    var chatContainerDiv = $("#chat_container");
    var chatContainerMarkerDiv = $("#chat_container_marker");
    var maxHeight = 2147483647;

    var addSelfMsg = function(from, to, content, date) {
        NRS.sendRequest("sendXBridgeMessage",
            {
                from:from,
                to: to,
                content: content,
                date: date
            },
            function(response){
                console.log(response);

                var msg = "<div class='chat-self-msg'>" + "<b>" + from + " => " + to + "</b> (" + date + ") <div>" + content +"</div></div>";
                $(msg).insertBefore(chatContainerMarkerDiv);

                chatContainerDiv.animate({
                    scrollTop: maxHeight
                }, 10);
            });
    };

    if(NRS['receiveXBridgeMessagesTimer'])
        clearInterval(NRS['receiveXBridgeMessagesTimer']);

    NRS['receiveXBridgeMessagesTimer'] = setInterval(function() {
        NRS.sendRequest("receiveXBridgeMessages",
            { },
            function(response){
                $.each(response['msgs'], function( index, value ) {
                    var from = value['from'],
                        to = value['to'],
                        content = value['content'],
                        date = new Date(value['date']);

                    var msg = "<div class='chat-incoming-msg'>" + "<b>" + from + " => " + to + "</b> (" + date + ") <div>" + content +"</div></div>";
                    $(msg).insertBefore(chatContainerMarkerDiv);

                    chatContainerDiv.animate({
                        scrollTop: maxHeight
                    }, 10);
                });
            });

    }, 3000);

    $("#send_chat_message_button").click(function(e) {
        var chatMessageText = $('#chat_message_text').val();
        $('#chat_message_text').val(null);

        var chatTarget = $('#chat_target').val();

        var accountId = $("#account_id").text();

        if(chatMessageText) {
            addSelfMsg(accountId, chatTarget, chatMessageText, new Date().getTime());
        }
    });

    return NRS;
}(NRS || {}, jQuery));