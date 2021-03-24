import React from 'react';
import DateSelect from './DateSelect';
import LanguageSelect from './LanguageSelect';


interface Props {
    setFullQuery: React.Dispatch<React.SetStateAction<string>>;
    query: string;
    language: string;
    setLanguage: React.Dispatch<React.SetStateAction<string>>;
    date: string;
    setDate: React.Dispatch<React.SetStateAction<string>>;
    totalResults: number;
};

const Subheader: React.FC<Props> = ({ setFullQuery, query, language, setLanguage, date, setDate, totalResults }) => {
    return (
        <>
            <div style={{display: 'inline-block', marginLeft: 50, marginRight: 50}}>
                {totalResults === 1 ? `1 total result` : `${totalResults} total results`}
            </div>
            {/* <LanguageSelect setFullQuery={setFullQuery} query={query} language={language} setLanguage={setLanguage} /> */}
            {/* <DateSelect setFullQuery={setFullQuery} query={query} date={date} setDate={setDate} /> */}
        </>
    );
};

export default Subheader;
