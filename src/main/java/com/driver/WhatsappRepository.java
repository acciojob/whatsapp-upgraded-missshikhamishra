package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {
    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository() {
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) {

        if (userMobile.contains(mobile)) {
            return "User already exists";
        }

        User user = new User(name, mobile);
        userMobile.add(mobile);

        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {

        // user is only 1, no chat or group
        if (users.size() < 2) {
            return null;
        }

        // users are 2, this is a personal chat
        else if (users.size() == 2) {
            Group group = new Group(users.get(1).getName(), 2);
            groupUserMap.put(group, users);

            return group;
        }

        // users > 2, its a group
        else {
            Group group = new Group("Group " + ++customGroupCount, users.size());
            groupUserMap.put(group, users);
            // add group admin in adminMap
            adminMap.put(group, users.get(0));
            return group;
        }
    }

    public int createMessage(String content) {

        Message message = new Message(++messageId, content);

        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) {

        // if group is not in groups list, throw error
        if (!groupUserMap.containsKey(group)) {
            return -1;
        }

        // user is not in that group, return user not exist
        if (!groupUserMap.get(group).contains(sender)) {
            return -2;
        }

        // if both group and user exists
        if (groupUserMap.containsKey(group) && groupUserMap.get(group).contains(sender)) {

            // check if group already have messages or we have to send the message for the very first time
            if (groupMessageMap.containsKey(group)) {
                // 1. send message in the group - add message to group message hashmap
                groupMessageMap.get(group).add(message);
            } else {
                List<Message> messages = new ArrayList<>();
                messages.add(message);
                groupMessageMap.put(group, messages);
            }

        }

        // 2. add message, sender to the message user hashmap
        senderMap.put(message, sender);

        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) {

        // check if the group exists, return with error message if yes
        if (!groupUserMap.containsKey(group)) {
            return "Group does not exist";
        }
        // check if the approver is current admin of the group, if not, return error message
        if (!adminMap.get(group).equals(approver)) {
            return "Approver does not have rights";
        }
        // check if the user exists in the group
        if (!groupUserMap.get(group).contains(user)) {
            return "User is not a participant";
        }

        adminMap.put(group, user);
        return "SUCCESS";
    }

    public int removeUser(User user) {

        Group group = null;

        // find user in every group
        for (Group group1 : groupUserMap.keySet()) {
            if (groupUserMap.get(group1).contains(user)) {
                group = group1;
                break;
            }
        }

        if (group == null) {
            return -1;
        }

        if (adminMap.get(group).equals(user)) {
            return -2;
        }

        // remove user from the group - groupUserMap
        groupUserMap.get(group).remove(user);

        // remove all of his messages from the group and also senderMap
        List<Message> messages = new ArrayList<>();
        // storing all the messages of the user
        for (Message message : senderMap.keySet()) {

            if (senderMap.get(message).equals(user)) {
                messages.add(message);
            }
        }

        // now delete messages from senderMap and groupMessageMap
        for (Message message : messages) {

            senderMap.remove(message);
            groupMessageMap.get(group).remove(message);
        }

        return group.getNumberOfParticipants() + groupMessageMap.get(group).size() + senderMap.size();
    }

    public String findMessage(Date start, Date end, int K) {

        List<Message> messages = new ArrayList<>();

        for (Message message : senderMap.keySet()) {

            if (message.getTimestamp().compareTo(start) > 0 && message.getTimestamp().compareTo(end) < 0) {
                messages.add(message);
            }
        }

        // if messages are less than K
        if (messages.size() < K) return "K is greater than the number of messages";

        return messages.get(messages.size() - K).getContent();

    }
}
