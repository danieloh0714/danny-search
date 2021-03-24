import React from 'react';


interface Props {
    query: string;
    txt: string;
};

const TextSnippet: React.FC<Props> = ({ query, txt }) => {
    const queryWords = query.split(" ");
    const firstQueryWord = queryWords[0].toLowerCase();

    const getFirstIdx = () => {
        let firstIdx = 0;
        const txtWords = txt.split(" ");
        for (let word of txtWords) {
            if (firstQueryWord === word.toLowerCase()) {
                return firstIdx;
            } else {
                firstIdx += word.length + 1;
            }
        }
        return textSnippet.toLowerCase().indexOf(firstQueryWord);
    };

    let textSnippet = txt;
    if (textSnippet.length > 200) {
        const idx = getFirstIdx();
        if (idx < 200 - firstQueryWord.length) {
            textSnippet = `${textSnippet.substring(0, 200)}...`;
        } else {
            textSnippet = `... ${textSnippet.substring(idx, idx + 200)}...`;
        }
    }

    const isQueryWord = (word: string) => {
        for (let queryWord of queryWords) {
            if (word.toLowerCase() === queryWord.toLowerCase()) {
                return true;
            }
        }
        return false;
    };

    const createMarkup = () => {
        const htmlMarkup = [];
        const textSnippetWords = textSnippet.split(" ");
        for (let word of textSnippetWords) {
            if (isQueryWord(word)) {
                htmlMarkup.push(`<b>${word}</b>`);
            } else {
                htmlMarkup.push(word);
            }
        }
        return {__html: htmlMarkup.join(" ")};
    };

    return (
        <div style={{fontSize: 15}}>
            <div dangerouslySetInnerHTML={createMarkup()} />
        </div>
    );
};

export default TextSnippet;
