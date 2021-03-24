import React from 'react';


interface Props {
    setFullQuery: React.Dispatch<React.SetStateAction<string>>;
    query: string;
    date: string;
    setDate: React.Dispatch<React.SetStateAction<string>>;
};

const DateSelect: React.FC<Props> = ({ setFullQuery, query, date, setDate }) => {
    const handleOnChange = (event: any) => {
        setFullQuery(`${query}&date=${event.target.value}`);
        setDate(event.target.value);
    };
    return (
        <input type="date" id="date-picker" value={date} onChange={event => handleOnChange(event)} />
    );
};

export default DateSelect;
