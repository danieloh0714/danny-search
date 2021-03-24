import React, { useEffect, useState } from 'react';
import Header from './components/Header';
import SearchResults from './components/SearchResults';
import Subheader from './components/Subheader';
import ViewMoreButton from './components/ViewMoreButton';


const App: React.FC = () => {
    const [fullQuery, setFullQuery] = useState<string>('');
    const [query, setQuery] = useState<string>('');
    const [language, setLanguage] = useState<string>('en');
    const [date, setDate] = useState<string>('');
    const [count, setCount] = useState<number>(10);
    const [totalResults, setTotalResults] = useState<number>(0);
    const [articles, setArticles] = useState<Array<any>>([]);

    const setSearchResults = (resultObj: { total_results: React.SetStateAction<number>; articles: React.SetStateAction<any[]>; }) => {
        setTotalResults(resultObj.total_results);
        setArticles(resultObj.articles);
    };

    useEffect(() => {
        const getSearchResults = () => {
            fetch(`http://searchapi-env.eba-famjqm4a.us-east-2.elasticbeanstalk.com/api/search?query=${fullQuery}`)
            .then(response => response.json())
            .then(resultObj => setSearchResults(resultObj))
            .catch(() => setArticles([]));
        };
        getSearchResults();
    }, [fullQuery]);

    return (
        <>
            <Header
                setFullQuery={setFullQuery}
                setQuery={setQuery}
                language={language}
                setDate={setDate}
                setCount={setCount}
            />
            <Subheader
                setFullQuery={setFullQuery}
                query={query}
                language={language}
                setLanguage={setLanguage}
                date={date}
                setDate={setDate}
                totalResults={totalResults}
            />
            <SearchResults query={query} articles={articles} />
            {
            articles.length === 0
            ?
            null
            :
            <ViewMoreButton
                setFullQuery={setFullQuery}
                query={query}
                count={count}
                setCount={setCount}
                totalResults={totalResults}
                articles={articles}
            />
            }
        </>
    );
};

export default App;
