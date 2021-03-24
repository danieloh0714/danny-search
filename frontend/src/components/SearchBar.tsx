import React from 'react';


interface Props {
    setFullQuery: React.Dispatch<React.SetStateAction<string>>;
    setQuery: React.Dispatch<React.SetStateAction<string>>;
    language: string;
    setDate: React.Dispatch<React.SetStateAction<string>>;
    setCount: React.Dispatch<React.SetStateAction<number>>;
};

const SearchBar: React.FC<Props> = ({ setFullQuery, setQuery, language, setDate, setCount }) => {
    const handleOnChange = (event: { target: { value: React.SetStateAction<string>; }; }) => {
        setFullQuery(`${event.target.value}&language=${language}`);
        setQuery(event.target.value);
        setDate('');
        setCount(10);
    };

    return (
        <input
            placeholder="Search..."
            onChange={event => handleOnChange(event)}
            style={{width: '30%', margin: 20, top: 100}}
        />
    );
};

export default SearchBar;
