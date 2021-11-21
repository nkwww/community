package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sun.text.normalizer.Trie;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤器
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 当容器实例化这个bean之后就会调用init方法
    @PostConstruct
    public void init() {
        // 从类路径下加载资源
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
        }
    }

    /**
     * 过滤敏感词
      * @param text 待过滤的文本
     * @return  过滤后的文本
     */
    public String filter (String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tem = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder stringBuilder = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)){
                // 若指针1处于根节点，将此符号计入结果，指针2向下走一步
                if (tem == rootNode) {
                    stringBuilder.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            // 检查下级节点
            tem = tem.getSubNode(c);
            if (tem == null) {
                // 以begin开头的字符串不是敏感词,将begin指向的字符加到结果中
                stringBuilder.append(text.charAt(begin));
                // begin指针右移一位，position指针随之移动
                position = ++begin;
                tem = rootNode;
            } else if (tem.isKeyWordEnd()) {
                // 发现敏感词，将begin ~ position之间的字符串替换掉
                stringBuilder.append(REPLACEMENT);
                // 进入position的下一个位置开始判断
                begin = ++position;
                tem = rootNode;
            } else {
                // 继续检查下一个字符
                position++;
            }
        }

        // 在循环之外将最后一批字符记入结果
        stringBuilder.append(text.substring(begin));

        return stringBuilder.toString();
    }

    private boolean isSymbol(Character character) {
        // 0x2E80 ~ 0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2E80 || character > 0x9FFF);
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword (String keyword) {
        TrieNode tem = rootNode;
        char[] chars = keyword.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            TrieNode subNode = tem.getSubNode(c);

            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tem.addSubNode(c, subNode);
            }

            // 指向子节点，进入下一轮循环
            tem = subNode;

            // 设置结束标识
            if (i == chars.length - 1) {
                tem.setKeyWordEnd(true);
            }
        }
    }

    // 前缀树节点
    private class TrieNode {
        // 关键词结束标志
        private boolean isKeyWordEnd = false;

        // 子节点(key是下级字符，value是下级节点)
        // 可以用 char[26]代替
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获得子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }



}
